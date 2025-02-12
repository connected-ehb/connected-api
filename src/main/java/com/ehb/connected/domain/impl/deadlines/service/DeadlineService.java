package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;

import java.util.List;

public interface DeadlineService {
    List<Deadline> getAllDeadlinesByAssignmentId(Long assignmentId);
    List<Deadline> getAllDeadlinesByAssignmentIdAndRestrictions(Long assignmentId, DeadlineRestriction restriction);
    Deadline getDeadlineById(Long id);
    Deadline createDeadline(Deadline deadline);
    Deadline updateDeadline(Deadline deadline);
    void deleteDeadline(Long id);

}
