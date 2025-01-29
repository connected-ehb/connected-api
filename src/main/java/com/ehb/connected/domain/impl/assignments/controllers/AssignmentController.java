package com.ehb.connected.domain.impl.assignments.controllers;


import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/{id}")
    public Assignment getAssignmentById(@PathVariable Long id){
        return assignmentService.getAssignmentById(id);
    }

    @PatchMapping("/{id}")
    public Assignment updateAssignment(@PathVariable Long id, Assignment assignment){
        return assignmentService.updateAssignment(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Long id){
        assignmentService.deleteAssignment(id);
    }



}
