package com.ehb.connected.domain.impl.assignments.service;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;

public interface AssignmentService {
    List<Assignment> getAllAssignments();
    Assignment getAssignmentById(Long id);
    Assignment createAssignment(Assignment assignment);
    Assignment updateAssignment(Assignment assignment);
    void deleteAssignment(Long id);

    List<Assignment> getAllAssignmentsByCourse(Long courseId);

    Assignment getAssignmentByCanvasAssignmentId(Long canvasAssignmentId);
    List<ProjectDetailsDto> publishAllProjects(Principal principal, Long assignmentId);
}
