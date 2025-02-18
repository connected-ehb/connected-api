package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
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
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;
    private final DeadlineService deadlineService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationMapper applicationMapper;

    private final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    public List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentId(assignmentId));
    }

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
     * Get a project by id
     * @param projectId the id of the project to get
     * @return ProjectDetailsDto
     */
    @Override
    public ProjectDetailsDto getProjectById(Long projectId) {
        return projectMapper.toDetailsDto(projectRepository.findById(projectId).orElseThrow(() ->
            new EntityNotFoundException(Project.class, projectId)
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
    public ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto project) {

        User user = projectUserService.getUser(principal);

        // Check if user has already created a project for this assignment
        if (projectRepository.existsByAssignmentIdAndMembersContainingAndStatusNotIn(assignmentId, user, List.of(ProjectStatusEnum.REJECTED))) {
            throw new RuntimeException("User has already created a project for this assignment");
        }

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
        System.out.println(assignment);
        // Fetch deadline for the assignment with the restriction PROJECT_CREATION
        Deadline deadline = deadlineService.getDeadlineByAssignmentIdAndRestrictions(assignmentId, DeadlineRestriction.PROJECT_CREATION);

        // If a deadline exists and has passed, throw an error
        if (deadline != null && deadline.getDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Project creation is no longer allowed. The deadline has passed.");
        }

        // Proceed with project creation since there is no deadline or the deadline is valid
        Project newProject = new Project();
        newProject.setTitle(project.getTitle());
        newProject.setDescription(project.getDescription());
        newProject.setShortDescription(project.getShortDescription());
        newProject.setRepositoryUrl(project.getRepositoryUrl());
        newProject.setBoardUrl(project.getBoardUrl());
        newProject.setBackgroundImage(project.getBackgroundImage());
        newProject.setTags(project.getTags());
        newProject.setStatus(ProjectStatusEnum.PENDING);
        // add current user to members list
        newProject.setMembers(List.of(user));

        newProject.setCreatedBy(projectUserService.getUser(principal));
        newProject.setAssignment(assignment);
        return projectMapper.toDetailsDto(projectRepository.save(newProject));
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
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // only pending projects can be updated
        if (existingProject.getStatus() != ProjectStatusEnum.PENDING) {
            throw new BaseRuntimeException("Project cannot be updated because it is no longer in the pending state.", HttpStatus.CONFLICT);
        }

        // Check if user is the owner of the project
        if (!projectUserService.isUserOwnerOfProject(principal, projectId)) {
            throw new RuntimeException("User is not the owner of the project");
        }

        existingProject.setTitle(project.getTitle());
        existingProject.setDescription(project.getDescription());
        existingProject.setRepositoryUrl(project.getRepositoryUrl());
        existingProject.setBackgroundImage(project.getBackgroundImage());

        return projectMapper.toDetailsDto(projectRepository.save(existingProject));
    }

    /**
     * Change the status of a project (only for teachers)
     * @param principal the principal of the user changing the status
     * @param projectId the id of the project to change the status of
     * @param status the new status of the project
     * @return ProjectDetailsDto
     */
    @Override
    public ProjectDetailsDto changeProjectStatus(Principal principal, Long id, ProjectStatusEnum status) {
        User user = projectUserService.getUser(principal);

        if (user.getRole().equals(Role.STUDENT)) {
            throw new RuntimeException("User is not authorized to change project status");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, id.toString()));
        project.setStatus(status);
        projectRepository.save(project);
        return projectMapper.toDetailsDto(project);
    }

    /**
     * Get all applications for a project (only for teachers and project owners)
     * @param principal the principal of the user getting the applications
     * @param projectId the id of the project for which to get the applications
     * @return List of ApplicationDto
     */
    @Override
    public List<ApplicationDto> getAllApplicationsByProjectId(Principal principal, Long projectId) {
        if (!projectUserService.isUserOwnerOfProject(principal, projectId) && projectUserService.getUser(principal).getRole().equals(Role.STUDENT)) {
            throw new RuntimeException("User is not the owner of the project");
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
        final Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        final User user = userService.getUserByPrincipal(principal);

        if (user.getRole().equals(Role.TEACHER)) {
            project.getMembers().removeIf(member -> member.getId().equals(memberId));
            projectRepository.save(project);
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
