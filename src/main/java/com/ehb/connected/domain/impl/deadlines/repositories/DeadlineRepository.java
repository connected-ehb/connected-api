package com.ehb.connected.domain.impl.deadlines.repositories;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeadlineRepository extends JpaRepository<Deadline, Long> {
    List<Deadline> findAllByAssignmentId(Long assignmentId);

    Deadline findTopByAssignmentIdAndRestrictionOrderByDateTimeDesc(Long assignmentId, DeadlineRestriction restriction);

}
