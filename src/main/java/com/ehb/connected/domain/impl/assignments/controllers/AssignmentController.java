package com.ehb.connected.domain.impl.assignments.controllers;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final ApplicationService applicationService;

    @PostMapping("/canvas/{courseId}")
    public ResponseEntity<List<AssignmentDetailsDto>> getAssignmentsFromCanvas(Principal principal, @PathVariable Long courseId) {
        List<AssignmentDetailsDto> filteredAssignmentsJson = assignmentService.getNewAssignmentsFromCanvas(principal, courseId);
        return ResponseEntity.ok(filteredAssignmentsJson);
    }

    @PostMapping("/")
    public ResponseEntity<AssignmentDetailsDto> createAssignment(@RequestBody AssignmentCreateDto assignmentDto) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignmentDto));
    }

    @GetMapping("/{assignmentId}/applications")
    public ResponseEntity<List<ApplicationDetailsDto>> getAllApplications(Principal principal, @PathVariable Long assignmentId){
        return ResponseEntity.ok(applicationService.getAllApplications(principal, assignmentId));
    }
}
