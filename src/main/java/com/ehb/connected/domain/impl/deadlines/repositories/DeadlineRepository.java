package com.ehb.connected.domain.impl.deadlines.repositories;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {

    @Query("SELECT d FROM Deadline d WHERE d.assignment.id = :assignmentId AND d.dueDate > :now ORDER BY d.dueDate ASC")
    List<Deadline> findUpcomingDeadlines(@Param("assignmentId") Long assignmentId, @Param("now") LocalDateTime now);

    Deadline findTopByAssignmentIdAndRestrictionOrderByDueDateDesc(Long assignmentId, DeadlineRestriction restriction);

}
