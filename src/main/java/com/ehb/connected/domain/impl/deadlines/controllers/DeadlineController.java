package com.ehb.connected.domain.impl.deadlines.controllers;


import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @PreAuthorize("hasAuthority('deadline:read')")
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<DeadlineDetailsDto>> getAllDeadlinesForAssignmentId(@PathVariable Long assignmentId){
        return ResponseEntity.ok(deadlineService.getAllDeadlinesByAssignmentId(assignmentId));
    }

    @PreAuthorize("hasAuthority('deadline:read')")
    @GetMapping("/{deadlineId}")
    public ResponseEntity<DeadlineDetailsDto> getDeadlineById(@PathVariable Long deadlineId) {
        return ResponseEntity.ok(deadlineService.getDeadlineById(deadlineId));
    }

    @PreAuthorize("hasAuthority('deadline:create')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<DeadlineDetailsDto> createDeadline(@PathVariable Long assignmentId, @RequestBody DeadlineCreateDto deadlineDto) {
        return ResponseEntity.ok(deadlineService.createDeadline(assignmentId, deadlineDto));
    }

    @PreAuthorize("hasAuthority('deadline:update')")
    @PatchMapping("/{deadlineId}")
    public ResponseEntity<DeadlineDetailsDto> updateDeadline(@PathVariable Long deadlineId, @RequestBody DeadlineUpdateDto deadlineDto) {
        return ResponseEntity.ok(deadlineService.updateDeadline(deadlineId, deadlineDto));
    }

    @PreAuthorize("hasAuthority('deadline:delete')")
    @DeleteMapping("/{deadlineId}")
    public ResponseEntity<Void> deleteDeadline(@PathVariable Long deadlineId) {
        deadlineService.deleteDeadline(deadlineId);
        return ResponseEntity.noContent().build();
    }
}
