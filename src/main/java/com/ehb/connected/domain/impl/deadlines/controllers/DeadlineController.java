package com.ehb.connected.domain.impl.deadlines.controllers;


import com.ehb.connected.domain.impl.deadlines.entities.Deadline;
import com.ehb.connected.domain.impl.deadlines.service.DeadlineService;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @GetMapping("/{assignmentId}")
    public List<Deadline> getAllDeadlines(@PathVariable Long assignmentId){
        return deadlineService.getAllDeadlinesByAssignmentId(assignmentId);
    }

    @GetMapping("/{deadlineId}")
    public Deadline getDeadlineById(@PathVariable Long deadlineId) {
        return deadlineService.getDeadlineById(deadlineId);
    }

    @PostMapping()
    public Deadline createDeadline(@RequestBody Deadline deadline) {
        return deadlineService.createDeadline(deadline);
    }

    @PatchMapping("/{deadlineId}")
    public Deadline updateDeadline(@PathVariable Long deadlineId, @RequestBody Deadline deadline) {
        return deadlineService.updateDeadline(deadline);
    }

    @DeleteMapping("/{deadlineId}")
    public void deleteDeadline(@PathVariable Long deadlineId) {
        deadlineService.deleteDeadline(deadlineId);
        return ResponseEntity.noContent().build();
    }
}
