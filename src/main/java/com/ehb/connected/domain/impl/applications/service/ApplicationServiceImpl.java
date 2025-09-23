package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.helpers.UrlHelper;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
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

    private final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    /**
     * Retrieves an application by its ID and verifies that the requesting user has the appropriate access rights.
     * <p>
     * A user is authorized to access the application if they meet any of the following criteria:
     * <ul>
     *   <li>They are the creator of the project associated with the application.</li>
     *   <li>They are the applicant who submitted the application.</li>
     *   <li>They have a teacher role.</li>
     * </ul>
     * </p>
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param applicationId the unique identifier of the application to retrieve.
     * @return an {@link ApplicationDetailsDto} containing the application details.
     * @throws EntityNotFoundException if no application with the given ID exists.
     * @throws UserUnauthorizedException if the user does not have sufficient permissions to view the application.
     */
    @Override
    public ApplicationDetailsDto getApplicationById(Principal principal, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(Application.class, applicationId));
        User user = userService.getUserFromAnyPrincipal(principal);
        // Checks if the user has access to the application
        if (user.equals(application.getProject().getProductOwner()) ||
                user.equals(application.getApplicant()) ||
                user.getRole() == Role.TEACHER) {
            return applicationMapper.toDto(application);
        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    /**
     * Creates a new application for a specified project, ensuring that the requesting user meets all requirements for submission.
     * <p>
     * The following validations are performed before an application is created:
     * <ul>
     *   <li>The user must have a student role.</li>
     *   <li>The target project must be in a published state.</li>
     *   <li>A valid submission deadline must not have passed for the assignment associated with the project.</li>
     *   <li>The user must not already be a member of any project in the assignment.</li>
     *   <li>The user must not have already applied to this project.</li>
     * </ul>
     * If any of these conditions are not met, the appropriate exception is thrown.
     * </p>
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param projectId the unique identifier of the project for which the application is being created.
     * @param applicationDto the data transfer object containing the details of the application, such as the motivation in Markdown.
     * @return an {@link ApplicationDetailsDto} containing the details of the newly created application.
     * @throws UserUnauthorizedException if the user does not have a student role.
     * @throws BaseRuntimeException if the project is not published, the user is already a member of a project in the assignment,
     *         or if the user has already applied to the project.
     * @throws DeadlineExpiredException if the application submission deadline for the assignment has passed.
     * @throws EntityNotFoundException if the target project cannot be found.
     */
    @Override
    public ApplicationDetailsDto createApplication(Principal principal, Long projectId, ApplicationCreateDto applicationDto) {

        final Project project = projectService.getProjectById(projectId);
        final User currentUser = userService.getUserFromAnyPrincipal(principal);

        if (currentUser.getRole() != Role.STUDENT) {
            throw new UserUnauthorizedException(currentUser.getId());
        }

        if (project.getStatus() != ProjectStatusEnum.PUBLISHED) {
            throw new BaseRuntimeException("Project is not published", HttpStatus.CONFLICT);
        }

        DeadlineDetailsDto deadlineDto = deadlineService.getDeadlineByAssignmentIdAndRestrictions(project.getAssignment().getId(), DeadlineRestriction.APPLICATION_SUBMISSION);
        if (deadlineDto != null && deadlineDto.getDueDate().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
            throw new DeadlineExpiredException(DeadlineRestriction.PROJECT_CREATION);
        }

        // Check if user has already created or joined a project in this assignment
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(principal, project.getAssignment().getId())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        //if the product owner is null, set the current user as the product owner
        //AND set all other application status for other projects to rejected
        if(project.getProductOwner() == null){
            project.setProductOwner(currentUser);
            currentUser.getApplications().stream()
                    .filter(app -> app.getProject().getAssignment().getId().equals(project.getAssignment().getId()))
                    .forEach(app -> {
                        if(app.getStatus() == ApplicationStatusEnum.PENDING){
                            app.setStatus(ApplicationStatusEnum.REJECTED);
                            applicationRepository.save(app);
                        }
                    });
            projectService.updateProject(project);
            //return null because don't need to create an application
            return null;
        }

        // Check if user has already applied to the project
        if (project.getApplications().stream().anyMatch(app -> app.getApplicant().equals(currentUser))) {
            throw new BaseRuntimeException("User has already applied to this project", HttpStatus.CONFLICT);
        }

        Application newApplication = new Application();
        newApplication.setStatus(ApplicationStatusEnum.PENDING);
        newApplication.setMotivationMd(applicationDto.getMotivationMd());
        newApplication.setApplicant(currentUser);
        newApplication.setProject(project);
        applicationRepository.save(newApplication);
        logger.info("[{}] Application has been created for project [{}]", ApplicationService.class.getSimpleName(), project.getId());



        // Check if receiver exits and send notification
        if (project.getProductOwner() != null) {
            String destinationUrl = UrlHelper.BuildCourseAssignmentUrl(
                    UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                    UrlHelper.Sluggify(project.getAssignment().getName()),
                    "projects", project.getId().toString(),
                    "applications");

            notificationService.createNotification(
                    project.getProductOwner(),
                    currentUser.getFirstName() + " " + currentUser.getLastName() + " applied for your project.",
                    destinationUrl
            );
        }

        return applicationMapper.toDto(newApplication);
    }

    /**
     * Retrieves a list of application details for a given assignment, with the results tailored to the role of the requesting user.
     * <p>
     * For a user with the STUDENT role, the method returns applications that the user has either submitted or received
     * on projects that the user owns. For a user with the TEACHER role, it returns all applications associated with the
     * specified assignment. If the user does not have one of these roles, a {@link UserUnauthorizedException} is thrown.
     * </p>
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param assignmentId the unique identifier of the assignment for which applications are to be retrieved.
     * @return a list of {@link ApplicationDetailsDto} objects representing the applications for the assignment.
     * @throws UserUnauthorizedException if the user does not have the STUDENT or TEACHER role.
     */
    @Override
    public List<ApplicationDetailsDto> getAllApplications(Principal principal, Long assignmentId) {
        User user = userService.getUserByPrincipal(principal);
        if(user.getRole() == Role.STUDENT){
            return applicationMapper.toDtoList(applicationRepository.findAllApplicationsByUserIdOrProjectProductOwnerAndAssignment(user.getId(), assignmentId));
        } else if(user.getRole() == Role.TEACHER){
            return applicationMapper.toDtoList(applicationRepository.findAllApplicationsByAssignmentId(assignmentId));
        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    /**
     * Reviews an application by updating its status, ensuring that the requesting user is authorized and the application is still pending.
     * <p>
     * The method performs the following steps:
     * <ol>
     *   <li>Retrieves the application by its ID, throwing an {@link EntityNotFoundException} if not found.</li>
     *   <li>Verifies that the user owns the project associated with the application; otherwise, throws a {@link UserNotOwnerOfProjectException}.</li>
     *   <li>Checks that the application is still pending; if it has already been reviewed, throws a {@link BaseRuntimeException} with a conflict status.</li>
     *   <li>If the new status is {@code APPROVED}, it rejects all other pending applications for the same applicant by calling {@code rejectAllOtherApplications()}.</li>
     *   <li>Sets the application's status to the provided value (approved or rejected) and saves the update.</li>
     * </ol>
     * </p>
     *
     * @param principal the security principal representing the currently authenticated user.
     * @param applicationId the unique identifier of the application to review.
     * @param status the new status to set for the application; typically either {@code APPROVED} or {@code REJECTED}.
     * @return an {@link ApplicationDetailsDto} representing the updated application.
     * @throws EntityNotFoundException if no application is found for the given ID.
     * @throws UserNotOwnerOfProjectException if the user is not the owner of the project associated with the application.
     * @throws BaseRuntimeException if the application has already been reviewed.
     */
    @Override
    @Transactional
    public ApplicationDetailsDto reviewApplication(Principal principal, Long applicationId, ApplicationStatusEnum status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> {
                    logger.error("Application not found for id: {}", applicationId);
                    return new EntityNotFoundException("Application not found");
                });

        // Ensure user owns the project
        if (!projectUserService.isUserOwnerOfProject(principal, application.getProject().getId())) {
            throw new UserNotOwnerOfProjectException();
        }

        // Prevent reviewing an already reviewed application
        if (application.getStatus() != ApplicationStatusEnum.PENDING) {
            throw new BaseRuntimeException("Application has already been reviewed", HttpStatus.CONFLICT);
        }

        Project project = application.getProject();

        // Set status (approved or rejected) and save
        application.setStatus(status);
        applicationRepository.save(application);

        // Check if receiver exits and send notification
        if (application.getApplicant() != null) {
            String destinationUrl = UrlHelper.BuildCourseAssignmentUrl(
                    UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                    UrlHelper.Sluggify(project.getAssignment().getName()),
                    "applications", application.getId().toString());

            notificationService.createNotification(
                    application.getApplicant(),
                    "your application for project " + project.getTitle() + " has been " + status.toString().toLowerCase(),
                    destinationUrl
            );
        }
        return applicationMapper.toDto(application);
    }

    @Override
    @Transactional
    public ApplicationDetailsDto joinProject(Principal principal, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(Application.class, applicationId));

        if (application.getStatus() != ApplicationStatusEnum.APPROVED) {
            throw new BaseRuntimeException("Application has not been approved", HttpStatus.CONFLICT);
        }
        User user = userService.getUserByPrincipal(principal);
        if (!user.equals(application.getApplicant())) {
            throw new UserUnauthorizedException(user.getId());
        }



        // Check if user is already member of another project
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(principal, application.getProject().getAssignment().getId())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        Project project = application.getProject();
        List<User> members = project.getMembers();
        //check if project is full
        if(members.size() >= project.getTeamSize()){
            throw new BaseRuntimeException("Project is full", HttpStatus.CONFLICT);
        }

        //reject all other applications for the same applicant
        rejectAllOtherApplications(application);
        members.add(user);
        project.setMembers(members);
        projectService.updateProject(project);

        logger.info("User [{}] has joined project [{}] based on approved application [{}]", user.getId(), project.getId(), applicationId);

        String destinationUrl = UrlHelper.BuildCourseAssignmentUrl(
                UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.Sluggify(project.getAssignment().getName()),
                "projects", project.getId().toString());

        notificationService.createNotification(
                user,
                "You have been added to project " + project.getTitle(),
                destinationUrl
        );

        return applicationMapper.toDto(application);
    }

    /**
     * Rejects all other pending applications for the same applicant as the provided application.
     *
     * @param application the application for which to reject other applications.
     */
    private void rejectAllOtherApplications(Application application) {
        List<Application> otherApplications = applicationRepository.findByApplicantInAssignment(application.getProject().getAssignment().getId(), application.getApplicant());
        otherApplications.stream()
                .filter(app -> !app.getId().equals(application.getId()))
                .forEach(app -> {
                    app.setStatus(ApplicationStatusEnum.REJECTED);
                    applicationRepository.save(app);
                });
    }
}
