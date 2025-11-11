package com.ehb.connected.domain.impl.projects.repositories;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.users.entities.Role;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByAssignmentId(Long assignmentId);

    @Query("SELECT p FROM Project p WHERE p.assignment.id = :assignmentId AND (p.status = :status OR p.productOwner = :user)")
    List<Project> findAllByAssignmentIdAndStatusOrOwnedBy(@Param("assignmentId") Long assignmentId,
                                                          @Param("status") ProjectStatusEnum status,
                                                          @Param("user") User user);

    Project findByMembersAndAssignmentIdAndStatus(List<User> users, Long assignmentId, ProjectStatusEnum status);

    List<Project> findAllByAssignmentIdAndStatus(Long assignmentId, ProjectStatusEnum status);

    boolean existsByAssignmentAndMembersContainingAndStatusNotIn(Assignment assignment, User user, List<ProjectStatusEnum> status);

    boolean existsByAssignmentIdAndGid(Long assignmentId, UUID gid);

    List<Project> findAllByAssignmentIdAndCreatedBy(Long assignmentId, User createdBy);

    List<Project> findAllByCreatedByRoleAndAssignmentIsNull(Role role);

    @Query("""
              select distinct p.createdBy.id
              from Project p
              where p.assignment.id = :assignmentId
                and p.status in ('APPROVED','PUBLISHED')
            """)
    Set<Long> findDistinctApprovedOwnerIds(@Param("assignmentId") Long assignmentId);

    @Query("""
              select p
              from Project p
              where p.assignment.id = :assignmentId
                and p.status in ('PENDING','REVISED')
            """)
    List<Project> findTopReviewQueue(@Param("assignmentId") Long assignmentId);

    @Query("""
              select p
              from Project p
              where p.assignment.id = :assignmentId
                and p.status = 'NEEDS_REVISION'
            """)
    List<Project> findTopNeedsRevision(@Param("assignmentId") Long assignmentId);

    int countByAssignmentIdAndStatusIn(Long assignmentId, Collection<ProjectStatusEnum> statuses);

    int countByAssignmentIdAndStatus(Long assignmentId, ProjectStatusEnum status);
           
    List<Project> findDistinctByCreatedBy_IdOrMembers_Id(Long createdById, Long memberId);

    List<Project> findAllByCreatedByAndAssignmentIsNull(User user);

    List<Project> findAllByCreatedByAndAssignmentIsNotNull(User user);
}
