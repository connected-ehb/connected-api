package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;

import java.security.Principal;
import java.util.List;

public interface AssignmentService {
    AssignmentDetailsDto createAssignment(AssignmentCreateDto assignmentDto);
    List<AssignmentDetailsDto> getAllAssignmentsByCourse(Long courseId);
    List<ProjectDetailsDto> publishAllProjects(Principal principal, Long assignmentId);
    List<AssignmentDetailsDto> getNewAssignmentsFromCanvas(Principal principal, Long courseId);
}
