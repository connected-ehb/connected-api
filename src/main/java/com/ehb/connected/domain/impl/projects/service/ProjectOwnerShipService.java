package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProjectOwnerShipService {
    private final ProjectRepository projectRepository;

    public ProjectOwnerShipService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public boolean isUserOwnerOfProject(Long projectId) {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return project.getCreatedBy().getId().equals(currentUser.getId());
    }
}
