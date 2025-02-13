package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final ProjectRepository projectRepository;
    private final UserServiceImpl userService;

    public boolean isUserOwnerOfProject(Principal principal, Long projectId) {

        User currentUser = userService.getUserByEmail(principal.getName());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        return project.getCreatedBy().getId().equals(currentUser.getId());
    }

    public User getUser(Principal principal) {
        return userService.getUserByEmail(principal.getName());
    }
}
