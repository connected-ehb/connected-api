package com.ehb.connected.domain.impl.projects.repositories;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByAssignmentId(Long assignmentId);
    List<Project> findAllByAssignmentIdAndStatus(Long AssignmentId, ProjectStatusEnum status);
    boolean existsByAssignmentIdAndMembersContainingAndStatusNotIn(Long assignmentId, User user, List<ProjectStatusEnum> status);

    boolean existsByMembersContainingAndStatusIn(User user, List<ProjectStatusEnum> pending);
}
