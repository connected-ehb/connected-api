package com.ehb.connected.domain.impl.projects.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.feedbacks.entities.Feedback;
import com.ehb.connected.domain.impl.feedbacks.entities.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.service.FeedbackService;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final FeedbackService feedbackService;
    private final ApplicationService applicationService;

    @GetMapping
    public List<ProjectDetailsDto> getAllProjects(@RequestHeader Long assignmentId){
        return projectService.getAllProjects(assignmentId);
    }

    @GetMapping("/{id}")
    public ProjectDetailsDto getProjectById(@PathVariable Long id){
        return projectService.getProjectById(id);
    }

    @PostMapping("/create")
    public ProjectDetailsDto createProject(Principal principal, @RequestHeader Long assignmentId, @RequestBody ProjectCreateDto project){
        return projectService.createProject(principal, assignmentId, project);
    }

    @PutMapping("/update/{id}")
    public ProjectDetailsDto updateProject(Principal principal, @PathVariable Long id, @RequestBody ProjectUpdateDto project){
        return projectService.updateProject(principal, id, project);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteProject(@PathVariable Long id){
        projectService.deleteProject(id);
    }


    @PatchMapping("/{id}/approve")
    public void approveProject(@PathVariable Long id) { projectService.approveProject(id);}

    @PatchMapping("/{id}/reject")
    public void rejectProject(@PathVariable Long id) { projectService.rejectProject(id);}

    @GetMapping("/{id}/applications")
    public List<ApplicationDto> getAllApplications(Principal principal, @PathVariable Long id) { return projectService.getAllApplications(principal, id);}

    @PostMapping("/{id}/applications/{applicationId}/approve")
    public void approveApplication(Principal principal, @PathVariable Long id, @PathVariable Long applicationId) {
        projectService.reviewApplication(principal, id, applicationId, "approve");
    }

    @PostMapping("/{id}/applications/{applicationId}/reject")
    public void rejectApplication(Principal principal, @PathVariable Long id, @PathVariable Long applicationId) {
        projectService.reviewApplication(principal, id, applicationId, "reject");
    }

    // Feedback endpoints
    @PostMapping("/{id}/feedback")
    public ResponseEntity<Feedback> giveFeedback(Principal principal, @PathVariable Long id, @RequestBody FeedbackDto feedbackDto) {
       return ResponseEntity.ok(feedbackService.giveFeedback(principal, id, feedbackDto));
    }

    @PutMapping("/{id}/feedback/{feedbackId}")
    public ResponseEntity<Feedback> updateFeedback(
            Principal principal,
            @PathVariable Long id,
            @PathVariable Long feedbackId,
            @RequestBody FeedbackDto feedbackDto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(principal, id, feedbackId, feedbackDto));
    }

    @DeleteMapping("/{id}/feedback/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(
            Principal principal,
            @PathVariable Long id,
            @PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(principal, id, feedbackId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<List<Feedback>> getFeedbacks(Principal principal, @PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackForProject(principal, id));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ApplicationDto> applyForProject(Principal principal, @PathVariable Long id, @RequestBody ApplicationCreateDto application) {
        return ResponseEntity.ok(applicationService.createApplication(principal, id, application));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public void removeMember(Principal principal, @PathVariable Long id, @PathVariable Long memberId) {
        projectService.removeMember(principal, id, memberId);
    }
}
