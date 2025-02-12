package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectUserService projectUserService;
    private final ProjectMapper projectMapper;
    private final DeadlineService deadlineService;

    @Override
    public List<ProjectDetailsDto> getAllProjects() {
        return projectMapper.toDetailsDtoList(projectRepository.findAll());
    }

    @Override
    public ProjectDetailsDto getProjectById(Long id) {
        return projectMapper.toDetailsDto(projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Project not found")));
    }

    @Override
    public ProjectDetailsDto createProject(ProjectCreateDto project) {
        // Fetch deadline for the assignment with the restriction PROJECT_CREATION
        Deadline deadline = deadlineService.getDeadlineByAssignmentIdAndRestrictions(project.getAssignmentId(), DeadlineRestriction.PROJECT_CREATION);

        // If a deadline exists and has passed, throw an error
        if (deadline != null && deadline.getDateTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Project creation is no longer allowed. The deadline has passed.");
        }

        // Proceed with project creation since there is no deadline or the deadline is valid
        Project newProject = projectMapper.toEntity(project);
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
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        project.setStatus(ProjectStatusEnum.APPROVED);
        projectRepository.save(project);
    }

    @Override
    public void rejectProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));
        project.setStatus(ProjectStatusEnum.REJECTED);
        projectRepository.save(project);
    }

    @Override
    public List<Application> getAllApplications(Principal principal, Long projectId) {
        if (!projectUserService.isUserOwnerOfProject(principal, projectId)) {
            throw new RuntimeException("User is not the owner of the project");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        return project.getApplications();
    }

    @Override
    public void reviewApplication(Principal principal, Long projectId, Long applicationId, String status) {
        // Check if user owns project
        if (!projectUserService.isUserOwnerOfProject(principal, projectId)) {
            throw new RuntimeException("User is not the owner of the project");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        // Ensure the application belongs to the project before updating it
        if (!Objects.equals(application.getProject().getId(), projectId)) {
            throw new RuntimeException("Application does not belong to the project");
        }

        if(Objects.equals(status, "approve")){
            application.setStatus(ApplicationStatusEnum.APPROVED);
        } else {
            application.setStatus(ApplicationStatusEnum.REJECTED);
        }

        applicationRepository.save(application);
    }
}
