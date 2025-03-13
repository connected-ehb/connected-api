package com.ehb.connected.domain.impl.deadlines.service;

import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;

import java.util.List;

public interface DeadlineService {
    List<DeadlineDetailsDto> getAllDeadlinesByAssignmentId(Long assignmentId);
    DeadlineDetailsDto getDeadlineByAssignmentIdAndRestrictions(Long assignmentId, DeadlineRestriction restriction);
    DeadlineDetailsDto getDeadlineById(Long deadlineId);
    DeadlineDetailsDto createDeadline(Long assignmentId, DeadlineCreateDto deadlineDto);
    DeadlineDetailsDto updateDeadline(Long deadlineId, DeadlineUpdateDto deadlineDto);
    void deleteDeadline(Long id);

}
