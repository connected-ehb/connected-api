package com.ehb.connected.domain.impl.applications.repositories;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE a.project.assignment.id = :id")
    List<Application> findAllApplicationsByAssignmentId(@Param("id") Long id);

    @Query("SELECT a FROM Application a WHERE a.project.assignment.id = :assignmentId AND a.applicant = :applicant")
    List<Application> findByApplicantInAssignment(@Param("assignmentId") Long assignmentId, @Param("applicant") User applicant);

    @Query("SELECT a FROM Application a WHERE (a.applicant.id = :userId OR a.project.productOwner.id = :userId) AND a.project.assignment.id = :assignmentId")
    List<Application> findAllApplicationsByUserIdOrProjectProductOwnerAndAssignment(@Param("userId") Long userId, @Param("assignmentId") Long assignmentId);

    @Query("""
              select distinct a.applicant.id
              from Application a
              join a.project p
              where p.assignment.id = :assignmentId
                and a.status = 'APPROVED'
            """)
    Set<Long> findDistinctApprovedApplicantIds(@Param("assignmentId") Long assignmentId);

    @Query("""
              select count(a)
              from Application a
              join a.project p
              where p.assignment.id = :assignmentId
                and a.status = 'PENDING'
            """)
    int countPendingByAssignment(@Param("assignmentId") Long assignmentId);
}
