package com.ehb.connected.domain.impl.projects.repositories;

import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByAssignmentId(Long assignmentId);

    @Query("SELECT p FROM Project p WHERE p.assignment.id = :assignmentId AND (p.status = :status OR p.productOwner = :user)")
    List<Project> findAllByAssignmentIdAndStatusOrOwnedBy(@Param("assignmentId") Long assignmentId,
                                                          @Param("status") ProjectStatusEnum status,
                                                          @Param("user") User user);

    List<Project> findAllByAssignmentIdAndStatus(Long assignmentId, ProjectStatusEnum status);
    boolean existsByAssignmentIdAndMembersContainingAndStatusNotIn(Long assignmentId, User user, List<ProjectStatusEnum> status);
}
