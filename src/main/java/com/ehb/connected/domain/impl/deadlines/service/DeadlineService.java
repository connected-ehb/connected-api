package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;

import java.util.List;

public interface DeadlineService {
    List<DeadlineDetailsDto> getAllDeadlinesByAssignmentId(Long assignmentId);
    Deadline getDeadlineByAssignmentAndRestrictions(Assignment assignment, DeadlineRestriction restriction);
    DeadlineDetailsDto getDeadlineById(Long deadlineId);
    DeadlineDetailsDto createDeadline(Long assignmentId, DeadlineCreateDto deadlineDto);
    DeadlineDetailsDto updateDeadline(Long deadlineId, DeadlineUpdateDto deadlineDto);
    void deleteDeadline(Long id);

}
