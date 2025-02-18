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
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.projects.service.ProjectUserService;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import com.ehb.connected.exceptions.BaseRuntimeException;
import com.ehb.connected.exceptions.DeadlineExpiredException;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.ehb.connected.exceptions.UserNotOwnerOfProjectException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final UserServiceImpl userService;

    private final ApplicationRepository applicationRepository;
    private final ProjectService projectService;
    private final DeadlineService deadlineService;
    private final ApplicationMapper applicationMapper;

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
        User user = userService.getUserByEmail(principal.getName());
        // Checks if the user has access to the application
        if (user.equals(application.getProject().getCreatedBy()) ||
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
        final User currentUser = userService.getUserByEmail(principal.getName());

        if (currentUser.getRole() != Role.STUDENT) {
            throw new UserUnauthorizedException(currentUser.getId());
        }

        if (project.getStatus() != ProjectStatusEnum.PUBLISHED) {
            throw new BaseRuntimeException("Project is not published", HttpStatus.CONFLICT);
        }

        Deadline deadline = deadlineService.getDeadlineByAssignmentIdAndRestrictions(project.getAssignment().getId(), DeadlineRestriction.APPLICATION_SUBMISSION);
        if (deadline != null && deadline.getDateTime().isBefore(LocalDateTime.now())) {
            throw new DeadlineExpiredException(DeadlineRestriction.APPLICATION_SUBMISSION);
        }

        // Check if user has already created or joined a project in this assignment
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(principal, project.getAssignment().getId())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        // Check if user has already applied to the project
        if (project.getApplications().stream().anyMatch(app -> app.getApplicant().equals(currentUser))) {
            throw new BaseRuntimeException("User has already applied to this project", HttpStatus.CONFLICT);
        }

        Application newApplication = new Application();
        newApplication.setStatus(ApplicationStatusEnum.PENDING);
        newApplication.setMotivationMd(application.getMotivationMd());
        newApplication.setApplicant(currentUser);
        newApplication.setProject(project);
        applicationRepository.save(newApplication);
        logger.info("[{}] Application has been created for project [{}]", ApplicationService.class.getSimpleName(), project.getId());
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
    public List<Application> findAllApplications(Long id) {
        return applicationRepository.findAllApplications(id);
    }

    @Override
    public List<Application> findAllApplicationsByUserAndAssignment(Long id, Long assignmentId) {
        return applicationRepository.findAllApplicationsByUserId(id, assignmentId);
    }

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
            logger.error("Application has already been reviewed");
            throw new BaseRuntimeException("Application has already been reviewed", HttpStatus.CONFLICT);
        }

        // If approving, reject all other pending applications for the same applicant
        if (status == ApplicationStatusEnum.APPROVED) {
            rejectAllOtherApplications(application);
        }

        // Set status (approved or rejected) and save
        application.setStatus(status);
        applicationRepository.save(application);
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
