package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.helpers.UrlHelper;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEventType;
import com.ehb.connected.domain.impl.projects.events.service.ProjectEventService;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import com.ehb.connected.domain.impl.projects.service.ProjectUserService;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.DeadlineExpiredException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.ehb.connected.exceptions.UserNotOwnerOfProjectException;
import com.ehb.connected.exceptions.UserUnauthorizedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserService userService;

    private final ApplicationRepository applicationRepository;
    private final ProjectService projectService;
    private final DeadlineService deadlineService;
    private final ApplicationMapper applicationMapper;
    private final NotificationServiceImpl notificationService;

    private final ProjectUserService projectUserService;
    private final ProjectEventService projectEventService;

    private final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    @Override
    public ApplicationDetailsDto getById(Authentication authentication, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(Application.class, applicationId));

        User user = userService.getUserByAuthentication(authentication);

        if (!hasAccessToApplication(user, application)) {
            throw new UserUnauthorizedException(user.getId());
        }

        return applicationMapper.toDto(application);
    }

    private boolean hasAccessToApplication(User user, Application application) {
        return user.isProductOwner(application.getProject()) || user.isApplicant(application) || user.hasRole(Role.TEACHER);
    }

    private void assertCanApply(User user, Project project) {
        if (!user.hasRole(Role.STUDENT)) {
            throw new UserUnauthorizedException(user.getId());
        }

        if (!project.hasStatus(ProjectStatusEnum.PUBLISHED)) {
            throw new BaseRuntimeException("Project is not published", HttpStatus.CONFLICT);
        }

        Deadline deadline = deadlineService.getDeadlineByAssignmentAndRestrictions(project.getAssignment(), DeadlineRestriction.APPLICATION_SUBMISSION);
        if (deadline != null && deadline.hasExpired()) {
            throw new DeadlineExpiredException(DeadlineRestriction.APPLICATION_SUBMISSION);
        }

        // Check if user has already created or joined a project in this assignment
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(user, project.getAssignment())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        if (project.hasNoMembers()) {
            throw new BaseRuntimeException("You can only claim this project", HttpStatus.CONFLICT);
        }
    }

    @Override
    public ApplicationDetailsDto create(Authentication authentication, Long projectId, ApplicationCreateDto applicationDto) {

        final Project project = projectService.getProjectById(projectId);
        final User user = userService.getUserByAuthentication(authentication);

        assertCanApply(user, project);

        if (project.hasUserApplied(user)) {
            throw new BaseRuntimeException("User has already applied to this project", HttpStatus.CONFLICT);
        }

        Application newApplication = new Application(null, applicationDto.getMotivationMd(), ApplicationStatusEnum.PENDING, project, user);
        applicationRepository.save(newApplication);

        projectEventService.logEvent(project.getId(), user.getId(), ProjectEventType.USER_APPLIED, "Application submitted");
        logger.info("[{}] Application has been created for project [{}]", ApplicationService.class.getSimpleName(), project.getId());

        // Check if receiver exits and send notification
        String destinationUrl = UrlHelper.buildCourseAssignmentUrl(
                UrlHelper.sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.sluggify(project.getAssignment().getName()),
                "projects",
                project.getId().toString(),
                "applications");

        notificationService.createNotification(
                project.getProductOwner(),
                user.getFullName() + " applied for your project.",
                destinationUrl
        );

        return applicationMapper.toDto(newApplication);
    }

    @Override
    public List<ApplicationDetailsDto> getAllApplications(Authentication authentication, Long assignmentId) {
        User user = userService.getUserByAuthentication(authentication);
        if (user.hasRole(Role.STUDENT)) {
            return applicationMapper.toDtoList(applicationRepository.findAllApplicationsByUserIdOrProjectProductOwnerAndAssignment(user.getId(), assignmentId));
        } else if (user.hasRole(Role.TEACHER)) {
            return applicationMapper.toDtoList(applicationRepository.findAllApplicationsByAssignmentId(assignmentId));
        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    @Override
    public ApplicationDetailsDto reviewApplication(Authentication authentication, Long applicationId, ApplicationStatusEnum status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(Application.class, applicationId));
        User user = userService.getUserByAuthentication(authentication);
        Project project = application.getProject();

        // Ensure user owns the project
        if (!user.isProductOwner(project)) {
            throw new UserNotOwnerOfProjectException();
        }

        // Prevent reviewing an already reviewed application
        if (!application.hasStatus(ApplicationStatusEnum.PENDING)) {
            throw new BaseRuntimeException("Application has already been reviewed", HttpStatus.CONFLICT);
        }

        // Set status (approved or rejected) and save
        application.setStatus(status);

        applicationRepository.save(application);

        // Check if receiver exits and send notification
        if (application.getApplicant() != null) {
            String destinationUrl = UrlHelper.buildCourseAssignmentUrl(
                    UrlHelper.sluggify(project.getAssignment().getCourse().getName()),
                    UrlHelper.sluggify(project.getAssignment().getName()),
                    "applications", application.getId().toString());

            notificationService.createNotification(
                    application.getApplicant(),
                    "your application for project " + project.getTitle() + " has been " + status.toString().toLowerCase(),
                    destinationUrl
            );
        }
        return applicationMapper.toDto(application);
    }

    @Transactional
    @Override
    public ApplicationDetailsDto joinProject(Authentication authentication, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(Application.class, applicationId));
        User user = userService.getUserByAuthentication(authentication);
        Project project = application.getProject();

        if (!application.hasStatus(ApplicationStatusEnum.APPROVED)) {
            throw new BaseRuntimeException("Application has not been approved", HttpStatus.CONFLICT);
        }

        if (!user.isApplicant(application)) {
            throw new UserUnauthorizedException(user.getId());
        }

        // Check if user is already member of another project
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(user, project.getAssignment())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        List<User> members = project.getMembers();
        //check if project is full
        if (project.hasReachedMaxMembers()) {
            throw new BaseRuntimeException("Project is full", HttpStatus.CONFLICT);
        }

        //reject all other applications for the same applicant
        rejectAllOtherApplications(application);
        members.add(user);
        project.setMembers(members);

        // If the project has now reached its maximum members, reject all remaining pending applications
        if (project.hasReachedMaxMembers()) {
            project.getApplications().stream()
                    .filter(app -> app.hasStatus(ApplicationStatusEnum.PENDING))
                    // TODO This will need a new status
                    .forEach(app -> app.setStatus(ApplicationStatusEnum.REJECTED));
        }

        projectEventService.logEvent(project.getId(), user.getId(), ProjectEventType.USER_JOINED, "Joined the project");

        projectService.save(project);

        logger.info("User [{}] has joined project [{}] based on approved application [{}]", user.getId(), project.getId(), applicationId);

        String destinationUrl = UrlHelper.buildCourseAssignmentUrl(
                UrlHelper.sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.sluggify(project.getAssignment().getName()),
                "projects", project.getId().toString());

        notificationService.createNotification(
                user,
                "You have been added to project " + project.getTitle(),
                destinationUrl
        );

        return applicationMapper.toDto(application);
    }

    private void rejectAllOtherApplications(Application application) {
        List<Application> otherApplications = applicationRepository.findByApplicantInAssignment(application.getProject().getAssignment().getId(), application.getApplicant());
        otherApplications.stream()
                .filter(otherApplication -> !otherApplication.equals(application))
                .forEach(otherApplication -> {
                    otherApplication.setStatus(ApplicationStatusEnum.REJECTED);
                    applicationRepository.save(otherApplication);
                });
    }

}
