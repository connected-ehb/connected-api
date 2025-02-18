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
    public List<ProjectDetailsDto> getAllPublishedProjectsByAssignmentId(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentIdAndStatus(assignmentId, ProjectStatusEnum.PUBLISHED));
    }

    @Override
    public ProjectDetailsDto getProjectById(Long projectId) {
        return projectMapper.toDetailsDto(projectRepository.findById(projectId).orElseThrow(() -> {
            logger.error("Project not found for id: {}", projectId);
            return new EntityNotFoundException(Project.class, projectId.toString());
        }));
    }

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

    @Override
    public ProjectDetailsDto updateProject(Principal principal, Long projectId, ProjectUpdateDto project) {
        Project existingProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // If the project is approved, return as it cannot be updated
        if (existingProject.getStatus() == ProjectStatusEnum.APPROVED) {
            return projectMapper.toDetailsDto(existingProject);
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


    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Override
    public void approveProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, id.toString()));
        project.setStatus(ProjectStatusEnum.APPROVED);
        projectRepository.save(project);
    }

    @Override
    public void rejectProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, id.toString()));
        project.setStatus(ProjectStatusEnum.REJECTED);
        projectRepository.save(project);
    }

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

    @Override
    public List<ApplicationDto> getAllApplicationsByProjectId(Principal principal, Long projectId) {
        if (!projectUserService.isUserOwnerOfProject(principal, projectId) && projectUserService.getUser(principal).getRole().equals(Role.STUDENT)) {
            throw new RuntimeException("User is not the owner of the project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId.toString()));

        return project.getApplications().stream()
                .map(applicationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeMember(Principal principal, Long id, Long memberId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        project.getMembers().removeIf(member -> member.getId().equals(memberId));
        projectRepository.save(project);
    }

    @Override
    public List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status) {
        return projectRepository.findAllByAssignmentIdAndStatus(assignmentId, status);
    }
}
