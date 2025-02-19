package com.ehb.connected.domain.impl.deadlines.controllers;


import com.ehb.connected.domain.impl.deadlines.dto.DeadlineCreateDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineDetailsDto;
import com.ehb.connected.domain.impl.deadlines.dto.DeadlineUpdateDto;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<DeadlineDetailsDto>> getAllDeadlinesForAssignmentId(@PathVariable Long assignmentId){
        return ResponseEntity.ok(deadlineService.getAllDeadlinesByAssignmentId(assignmentId));
    }

    @GetMapping("/{deadlineId}")
    public ResponseEntity<DeadlineDetailsDto> getDeadlineById(@PathVariable Long deadlineId) {
        return ResponseEntity.ok(deadlineService.getDeadlineById(deadlineId));
    }

    @PostMapping("/{assignmentId}")
    public ResponseEntity<DeadlineDetailsDto> createDeadline(@PathVariable Long assignmentId, @RequestBody DeadlineCreateDto deadlineDto) {
        return ResponseEntity.ok(deadlineService.createDeadline(assignmentId, deadlineDto));
    }

    @PatchMapping("/{deadlineId}")
    public ResponseEntity<DeadlineDetailsDto> updateDeadline(@PathVariable Long deadlineId, @RequestBody DeadlineUpdateDto deadlineDto) {
        return ResponseEntity.ok(deadlineService.updateDeadline(deadlineId, deadlineDto));
    }

    @DeleteMapping("/{deadlineId}")
    public ResponseEntity<Void> deleteDeadline(@PathVariable Long deadlineId) {
        deadlineService.deleteDeadline(deadlineId);
        return ResponseEntity.noContent().build();
    }
}
