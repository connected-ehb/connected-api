package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.mappers.ProjectMapper;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;
    private final TagMapper tagMapper;

    private final DeadlineService deadlineService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationMapper applicationMapper;
    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    /**
     * Get a project by id
     * @param projectId the id of the project to get
     * @return ProjectDetailsDto
     */
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

    /**
     * Get all projects for a specific assignment (For Teachers)
     * @param assignmentId the id of the assignment for which to get the projects
     * @return List of ProjectDetailsDto
     */
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
     * Create a project
     * @param principal the principal of the user creating the project
     * @param assignmentId the id of the assignment for which to create the project
     * @param projectDto the CreateDto containing the project data
     * @return ProjectDetailsDto
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
        final Deadline deadline = deadlineService.getDeadlineByAssignmentIdAndRestrictions(assignmentId, DeadlineRestriction.PROJECT_CREATION);

        // If a deadline exists and has passed, throw an error
        if (deadline != null && deadline.getDateTime().isBefore(LocalDateTime.now())) {
            throw new DeadlineExpiredException(DeadlineRestriction.PROJECT_CREATION);
        }

        Project newProject = projectMapper.toEntity(projectDto);
        newProject.setStatus(ProjectStatusEnum.PENDING);
        newProject.setMembers(List.of(user));
        newProject.setCreatedBy(userService.getUserByPrincipal(principal));
        newProject.setAssignment(assignment);
        Project savedProject = projectRepository.save(newProject);
        logger.info("[{}] Project has been created", ProjectService.class.getName());
        return projectMapper.toDetailsDto(savedProject);
    }

    /**
     * Update a project (only for project owners)
     * @param principal the principal of the user updating the project
     * @param projectId the id of the project to update
     * @param project the UpdateDto containing the new project data
     * @return ProjectDetailsDto
     */
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

        existingProject.setTitle(project.getTitle());
        existingProject.setDescription(project.getDescription());
        existingProject.setRepositoryUrl(project.getRepositoryUrl());
        existingProject.setBackgroundImage(project.getBackgroundImage());

        existingProject.setTags(tagMapper.toEntityList(project.getTags().stream().distinct().toList()));

        Project savedProject = projectRepository.save(existingProject);
        logger.info("[{}] Project with id: {} has been updated", ProjectService.class.getSimpleName(), projectId);

        return projectMapper.toDetailsDto(savedProject);
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
        return projectMapper.toDetailsDto(project);
    }

    /**
     * Get all applications for a project (only for teachers and project owners)
     * @param principal the principal of the user getting the applications
     * @param projectId the id of the project for which to get the applications
     * @return List of ApplicationDto
     */
    @Override
    public List<ApplicationDetailsDto> getAllApplicationsByProjectId(Principal principal, Long projectId) {
        if (projectUserService.isUserOwnerOfProject(principal, projectId) || userService.getUserByPrincipal(principal).getRole().equals(Role.TEACHER)) {
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
