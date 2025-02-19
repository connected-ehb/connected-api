package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;

import java.security.Principal;
import java.util.List;

public interface AssignmentService {
    AssignmentDetailsDto createAssignment(AssignmentCreateDto assignmentDto);
    Assignment getAssignmentById(Long assignmentId);
    List<AssignmentDetailsDto> getAllAssignmentsByCourse(Long courseId);
    List<AssignmentDetailsDto> getNewAssignmentsFromCanvas(Principal principal, Long courseId);
}
