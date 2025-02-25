package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.notifications.helpers.UrlHelper;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
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

import java.security.Principal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;

    private final DeadlineService deadlineService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;
    private final UrlHelper urlHelper;
    private final NotificationServiceImpl notificationService;

    private final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    public ProjectDetailsDto getProjectById(Principal principal, Long projectId) {
        User user = userService.getUserByPrincipal(principal);
        Project project = getProjectById(projectId);

        // Check if user is the owner of the project or a teacher and the project is not published
        if (project.getStatus() == ProjectStatusEnum.PUBLISHED) {
            return projectMapper.toDetailsDto(project);
        } else if (user.getRole().equals(Role.TEACHER) || projectUserService.isUserOwnerOfProject(principal, projectId)) {
            return projectMapper.toDetailsDto(project);
        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    @Override
    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));
    }

    @Override
    public List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentId(assignmentId));
    }

    /**
     * Get all published projects (For Students)
     * @param assignmentId the id of the assignment for which to get the published projects
     * @return List of ProjectDetailsDto
     */
    @Override
    public List<ProjectDetailsDto> getAllPublishedOrOwnedProjectsByAssignmentId(Principal principal, Long assignmentId) {
        return projectMapper.toDetailsDtoList(
                projectRepository.findAllByAssignmentIdAndStatusOrOwnedBy(
                        assignmentId,
                        ProjectStatusEnum.PUBLISHED,
                        userService.getUserByPrincipal(principal)
                ));
    }

    /**
     * Creates a new project for a specified assignment.
     * <p>
     * This method carries out the following steps:
     * <ol>
     *     <li>Retrieves the current user from the provided {@code Principal}.</li>
     *     <li>Checks if the user is already a member of any project associated with the specified assignment.
     *         If so, a {@link BaseRuntimeException} with a conflict status is thrown.</li>
     *     <li>Fetches the assignment using the provided assignment ID. If the assignment is not found, an
     *         {@link EntityNotFoundException} is thrown.</li>
     *     <li>For users with the role {@code STUDENT}, attempts to fetch the project creation deadline.
     *         If a deadline exists and has passed (relative to the current UTC time), a
     *         {@link DeadlineExpiredException} is thrown.
     *         If no deadline exists, the check is skipped.</li>
     *     <li>Maps the provided project data (from {@code ProjectCreateDto}) to a new project entity.</li>
     *     <li>Depending on the role of the user:
     *         <ul>
     *             <li>If the user is a student:
     *                 <ul>
     *                     <li>Sets the project status to {@link ProjectStatusEnum#PENDING}.</li>
     *                     <li>Assigns the user as a member and as the creator (product owner) of the project.</li>
     *                 </ul>
     *             </li>
     *             <li>If the user is a teacher:
     *                 <ul>
     *                     <li>Sets the project status to {@link ProjectStatusEnum#APPROVED}.</li>
     *                     <li>Leaves the members list empty, as teachers cannot be product owners.</li>
     *                 </ul>
     *             </li>
     *         </ul>
     *     </li>
     *     <li>Associates the project with the retrieved assignment and saves it to the repository.</li>
     *     <li>If the project is created by a student, notifies all teachers about the new project via the notification service.</li>
     * </ol>
     * </p>
     *
     * @param principal    the {@link Principal} representing the user creating the project.
     * @param assignmentId the unique identifier of the assignment for which the project is being created.
     * @param projectDto   the data transfer object containing the project details.
     * @return a {@link ProjectDetailsDto} representing the newly created project.
     * @throws BaseRuntimeException    if the user is already a member of a project for the specified assignment.
     * @throws EntityNotFoundException if the assignment with the given ID is not found.
     * @throws DeadlineExpiredException if the project creation deadline has expired for a student user.
     */


    @Override
    public ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto projectDto) {

        final User user = userService.getUserByPrincipal(principal);

        // Check if user has already created a project for this assignment
        if (projectUserService.isUserMemberOfAnyProjectInAssignment(principal, assignmentId)) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        final Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(Assignment.class, assignmentId));

        // Fetch deadline for the assignment with the restriction PROJECT_CREATION
        try {
            final DeadlineDetailsDto deadlineDto = deadlineService.getDeadlineByAssignmentIdAndRestrictions(assignmentId, DeadlineRestriction.PROJECT_CREATION);
            // If a deadline exists and has passed, throw an error
            //check if deadline is not null and if the deadline is before the current time IN UTC!!!!!
            if (user.getRole() == Role.STUDENT && deadlineDto != null && deadlineDto.getDueDate().isBefore(LocalDateTime.now(Clock.systemUTC()))) {
                throw new DeadlineExpiredException(DeadlineRestriction.PROJECT_CREATION);
            }
        } catch (EntityNotFoundException e) {
            logger.info("[{}] No project creation deadline found for assignment with ID: {}", ProjectService.class.getSimpleName(), assignmentId);
        }

        Project newProject = projectMapper.toEntity(projectDto);

        // Set the default team size from the assignment if not provided
        if (projectDto.getTeamSize() == null) {
            newProject.setTeamSize(assignment.getDefaultTeamSize());
        }


        // If user is a student, make him product owner, else if he is teacher leave it null as teacher cannot be product owner
        if(user.getRole() == Role.STUDENT) {
            newProject.setStatus(ProjectStatusEnum.PENDING);
            newProject.setMembers(List.of(user));
            newProject.setProductOwner(user);
        } else if (user.getRole() == Role.TEACHER) {
            newProject.setMembers(List.of());
            newProject.setStatus(ProjectStatusEnum.PUBLISHED);
        }

        newProject.setCreatedBy(user);
        newProject.setAssignment(assignment);
        Project savedProject = projectRepository.save(newProject);
        logger.info("[{}] Project has been created", ProjectService.class.getName());

        // Only if the creator of the project is a student do you notify the teachers
        if(user.getRole() == Role.STUDENT) {
            List<User> teachers = userService.getAllUsersByRole(Role.TEACHER);
            String destinationUrl = urlHelper.UrlBuilder(
                    UrlHelper.Sluggify(newProject.getAssignment().getCourse().getName()),
                    UrlHelper.Sluggify(newProject.getAssignment().getName()),
                    "projects/" + newProject.getId());

            for (User teacher : teachers) {
                notificationService.createNotification(
                        teacher,
                        String.format("A new project has been created: %s by %s %s",
                                newProject.getTitle(),
                                newProject.getCreatedBy().getFirstName(),
                                newProject.getCreatedBy().getLastName()),
                        destinationUrl
                );
        }

        }
        return projectMapper.toDetailsDto(savedProject);
    }

    @Override
    public ProjectDetailsDto updateProject(Principal principal, Long projectId, ProjectUpdateDto project) {
        final Project existingProject = getProjectById(projectId);

        // only pending projects can be updated
        if (existingProject.getStatus() != ProjectStatusEnum.PENDING) {
            throw new BaseRuntimeException("Project cannot be updated because it is no longer in the pending state.", HttpStatus.CONFLICT);
        }

        // Check if user is the owner of the project
        if (!projectUserService.isUserOwnerOfProject(principal, projectId)) {
            throw new UserNotOwnerOfProjectException();
        }

        projectMapper.updateEntityFromDto(project, existingProject);
        Project savedProject;
        try {
            savedProject = projectRepository.save(existingProject);
        } catch (Exception e) {
            logger.error("an error occurred while updating project with id: {}", projectId , e);
            throw new BaseRuntimeException("Project could not be updated", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.info("[{}] Project with id: {} has been updated", ProjectService.class.getSimpleName(), projectId);

        return projectMapper.toDetailsDto(savedProject);
    }

    @Override
    public void updateProject(Project project) {
        projectRepository.save(project);
    }

    /**
     * Change the status of a project (only for teachers)
     * @param principal the principal of the user changing the status
     * @param projectId the id of the project to change the status of
     * @param status the new status of the project
     * @return ProjectDetailsDto
     */
    @Override
    public ProjectDetailsDto changeProjectStatus(Principal principal, Long projectId, ProjectStatusEnum status) {
        final User user = userService.getUserByPrincipal(principal);

        if (user.getRole().equals(Role.STUDENT)) {
            throw new UserUnauthorizedException(user.getId());
        }

        final Project project = getProjectById(projectId);
        ProjectStatusEnum previousStatus = project.getStatus();
        project.setStatus(status);
        projectRepository.save(project);
        logger.info("[{}] Project ID: {} status changed from {} to {} by User ID: {}",
                ProjectService.class.getSimpleName(), projectId, previousStatus, status, user.getId());

        String destinationUrl = urlHelper.UrlBuilder(
                UrlHelper.Sluggify(project.getAssignment().getCourse().getName()),
                UrlHelper.Sluggify(project.getAssignment().getName()),
                "projects/" + project.getId());

        notificationService.createNotification(
                project.getProductOwner(),
                "Status for your project has been changed to: " + project.getStatus(),
                destinationUrl
        );

        return projectMapper.toDetailsDto(project);
    }

    @Override
    public List<ProjectDetailsDto> publishAllProjects(Principal principal, Long assignmentId) {
        final List<Project> projects = getAllProjectsByStatus(assignmentId, ProjectStatusEnum.APPROVED);
        projects.forEach(project -> changeProjectStatus(principal, project.getId(), ProjectStatusEnum.PUBLISHED));
        logger.info("All approved projects have been published.");
        return projectMapper.toDetailsDtoList(projects);
    }

    /**
     * Get all applications for a project (only for teachers and project owners)
     * @param principal the principal of the user getting the applications
     * @param projectId the id of the project for which to get the applications
     * @return List of ApplicationDto
     */
    @Override
    public List<ApplicationDetailsDto> getAllApplicationsByProjectId(Principal principal, Long projectId) {
        if (userService.getUserByPrincipal(principal).getRole().equals(Role.TEACHER) || projectUserService.isUserOwnerOfProject(principal, projectId)) {
            final Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

            return applicationMapper.toDtoList(project.getApplications());
        } else {
            throw new UserNotOwnerOfProjectException();
        }
    }


    /**
     * Remove a member from a project (only for teachers)
     * @param principal the principal of the user removing the member
     * @param projectId the id of the project from which to remove the member
     * @param memberId the id of the member to remove
     */
    @Override
    public void removeMember(Principal principal, Long projectId, Long memberId) {
        final Project project = getProjectById(projectId);

        final User user = userService.getUserByPrincipal(principal);

        if (user.getRole().equals(Role.TEACHER)) {
            final boolean removed = project.getMembers().removeIf(member -> member.getId().equals(memberId));
            if (removed) {
                if (projectUserService.isUserOwnerOfProject(memberId, projectId)) {
                    if (!project.getMembers().isEmpty()) {
                        project.setProductOwner(project.getMembers().get(0));
                    } else {
                        project.setProductOwner(null);
                    }
                }
                project.getApplications().stream()
                        .filter(application -> application.getApplicant().getId().equals(memberId))
                        .findFirst()
                        .ifPresent(application -> application.setStatus(ApplicationStatusEnum.REJECTED));
                logger.info("[{}] Member ID: {} was successfully removed from project ID: {} by User ID: {}",
                        ProjectService.class.getSimpleName(), memberId, projectId, user.getId());
                projectRepository.save(project);
            } else {
                throw new EntityNotFoundException(User.class, memberId);
            }
        } else {
            throw new UserUnauthorizedException(user.getId());
        }
    }

    @Override
    public ProjectDetailsDto claimProject(Principal principal, Long projectId) {
        final User user = userService.getUserByPrincipal(principal);
        final Project project = getProjectById(projectId);

        if (projectUserService.isUserMemberOfAnyProjectInAssignment(principal, project.getAssignment().getId())) {
            throw new BaseRuntimeException("User is already a member of a project in this assignment", HttpStatus.CONFLICT);
        }

        // reject all other applications of the user
        user.getApplications().stream()
                .filter(application -> application.getProject().getAssignment().getId().equals(project.getAssignment().getId()))
                .forEach(application -> application.setStatus(ApplicationStatusEnum.REJECTED));

        project.getMembers().add(user);
        project.setProductOwner(user);
        projectRepository.save(project);
        logger.info("[{}] Project ID: {} has been claimed by User ID: {}", ProjectService.class.getSimpleName(), projectId, user.getId());
        return projectMapper.toDetailsDto(project);
    }


    /**
     * Get all projects for a specific assignment with a specific status
     * @param assignmentId the id of the assignment for which to get the projects
     * @param status the status of the projects to get
     * @return List of Project
    */
    @Override
    public List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status) {
        return projectRepository.findAllByAssignmentIdAndStatus(assignmentId, status);
    }
}
