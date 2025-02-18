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
import org.springframework.http.ResponseEntity;
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
    private final ApplicationRepository applicationRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;
    private final DeadlineService deadlineService;
    private final AssignmentRepository assignmentRepository;
    private final ApplicationMapper applicationMapper;

    private final Logger logger = LoggerFactory.getLogger(ProjectServiceImpl.class);

    @Override
    public List<ProjectDetailsDto> getAllProjects(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentId(assignmentId));
    }

    @Override
    public List<ProjectDetailsDto> getAllPublishedProjectsInAssignment(Long assignmentId) {
        return projectMapper.toDetailsDtoList(projectRepository.findAllByAssignmentIdAndStatus(assignmentId, ProjectStatusEnum.PUBLISHED));
    }

    @Override
    public ProjectDetailsDto getProjectById(Long id) {
        return projectMapper.toDetailsDto(projectRepository.findById(id).orElseThrow(() -> {
            logger.error("Project not found for id: {}", id);
            return new EntityNotFoundException(Project.class, id.toString());
        }));
    }

    @Override
    public ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto project) {

        User user = projectUserService.getUser(principal);

        // Check if user has pending or approved projects. If so return an error
        if (projectRepository.existsByMembersContainingAndStatusIn(user, List.of(ProjectStatusEnum.PENDING, ProjectStatusEnum.APPROVED, ProjectStatusEnum.PUBLISHED))) {
            throw new RuntimeException("User already has a pending or approved or published project");
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
    public ProjectDetailsDto updateProject(Principal principal, Long id, ProjectUpdateDto project) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // If the project is approved, return as it cannot be updated
        if (existingProject.getStatus() == ProjectStatusEnum.APPROVED) {
            return projectMapper.toDetailsDto(existingProject);
        }

        // Check if user is the owner of the project
        if (!projectUserService.isUserOwnerOfProject(principal, id)) {
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
    public ResponseEntity<ProjectDetailsDto> changeProjectStatus(Principal principal, Long id, ProjectStatusEnum status) {
        User user = projectUserService.getUser(principal);

        if (user.getRole().equals(Role.STUDENT)) {
            return ResponseEntity.status(403).build();
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, id.toString()));
        project.setStatus(status);
        projectRepository.save(project);
        return ResponseEntity.ok(projectMapper.toDetailsDto(project));
    }

    @Override
    public List<ApplicationDto> getAllApplications(Principal principal, Long projectId) {
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
    @Transactional
    public void reviewApplication(Principal principal, Long projectId, Long applicationId, ApplicationStatusEnum status) {
        // Ensure user owns the project
        if (!projectUserService.isUserOwnerOfProject(principal, projectId)) {
            throw new RuntimeException("User is not the owner of the project");
        }

        // Ensure user is not member of another project

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        // Ensure the application belongs to the project
        if (!Objects.equals(application.getProject().getId(), projectId)) {
            throw new IllegalArgumentException("Application does not belong to the project");
        }

        // Prevent reviewing an already reviewed application
        if (isApplicationReviewed(application)) {
            throw new IllegalStateException("Application has already been reviewed");
        }

        if (status == ApplicationStatusEnum.APPROVED) {
            approveApplication(application, projectId);
        } else {
            application.setStatus(ApplicationStatusEnum.REJECTED);
            applicationRepository.save(application);
        }
    }

    private boolean isApplicationReviewed(Application application) {
        return application.getStatus() == ApplicationStatusEnum.APPROVED
                || application.getStatus() == ApplicationStatusEnum.REJECTED;
    }

    private void approveApplication(Application application, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId.toString()));

        // Ensure applicant is not already a member
        if (project.getMembers().stream().noneMatch(member -> member.getId().equals(application.getApplicant().getId()))) {
            project.getMembers().add(application.getApplicant());
            projectRepository.save(project);
        }

        // Reject other pending applications from the same applicant
        List<Application> otherApplications = applicationRepository.findByApplicantAndStatus(application.getApplicant(), ApplicationStatusEnum.PENDING);
        otherApplications.forEach(app -> {
            if (!app.getId().equals(application.getId())) {
                app.setStatus(ApplicationStatusEnum.REJECTED);
                applicationRepository.save(app);
            }
        });

        application.setStatus(ApplicationStatusEnum.APPROVED);
        applicationRepository.save(application);
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
