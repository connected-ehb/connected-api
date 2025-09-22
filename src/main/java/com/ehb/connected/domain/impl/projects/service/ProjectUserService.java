package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserServiceImpl;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final ProjectRepository projectRepository;
    private final UserServiceImpl userService;

    public boolean isUserOwnerOfProject(Principal principal, Long projectId) {

        User currentUser = userService.getUserByPrincipal(principal);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        return (currentUser.getRole().equals(Role.TEACHER) && project.getCreatedBy().equals(currentUser)) || project.getProductOwner().equals(currentUser);
    }

    public boolean isUserOwnerOfProject(long userId, Long projectId) {

        User currentUser = userService.getUserById(userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(Project.class, projectId));

        return project.getProductOwner().equals(currentUser);
    }

    public boolean isUserMemberOfAnyProjectInAssignment(Principal principal, Long assignmentId) {
        User currentUser = userService.getUserFromAnyPrincipal(principal);
        return projectRepository.existsByAssignmentIdAndMembersContainingAndStatusNotIn(assignmentId, currentUser, List.of(ProjectStatusEnum.REJECTED));
    }
}
