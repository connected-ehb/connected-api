package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.repositories.ApplicationRepository;
import com.ehb.connected.domain.impl.feedbacks.repositories.FeedbackRepository;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private final ProjectRepository projectRepository;

    @Autowired
    private final ApplicationRepository applicationRepository;

    @Autowired
    private final ProjectUserService projectUserService;

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Project not found"));
    }

    @Override
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(Long id, Project project) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        // If the project is approved, return as it cannot be updated
        if (existingProject.getStatus() == ProjectStatusEnum.APPROVED) {
            return existingProject;
        }

        existingProject.setTitle(project.getTitle());
        existingProject.setDescription(project.getDescription());
        existingProject.setRepositoryUrl(project.getRepositoryUrl());
        existingProject.setBackgroundImage(project.getBackgroundImage());

        return projectRepository.save(existingProject);
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
