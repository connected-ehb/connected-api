package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectUserService {

    private final ProjectRepository projectRepository;

    public boolean isUserMemberOfAnyProjectInAssignment(User user, Assignment assignment) {
        return projectRepository.existsByAssignmentAndMembersContainingAndStatusNotIn(assignment, user, List.of(ProjectStatusEnum.REJECTED));
    }
}
