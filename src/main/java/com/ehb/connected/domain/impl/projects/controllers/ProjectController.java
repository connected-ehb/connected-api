package com.ehb.connected.domain.impl.projects.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackCreateDto;
import com.ehb.connected.domain.impl.feedbacks.dto.FeedbackDto;
import com.ehb.connected.domain.impl.feedbacks.service.FeedbackService;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
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

    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<ProjectDetailsDto>> getAllProjects(@PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllProjectsByAssignmentId(assignmentId));
    }

    @GetMapping("/{assignmentId}/published")
    public ResponseEntity<List<ProjectDetailsDto>> getAllPublishedProjects(@PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllPublishedProjectsByAssignmentId(assignmentId));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> getProjectById(@PathVariable Long projectId){
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PostMapping("/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> createProject(Principal principal, @PathVariable Long assignmentId, @RequestBody ProjectCreateDto project){
        return ResponseEntity.ok(projectService.createProject(principal, assignmentId, project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> updateProject(Principal principal, @PathVariable Long projectId, @RequestBody ProjectUpdateDto project){
        return ResponseEntity.ok(projectService.updateProject(principal, projectId, project));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId){
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{projectId}/status")
    public ResponseEntity<ProjectDetailsDto> changeProjectStatus(Principal principal, @PathVariable Long projectId, @RequestHeader ProjectStatusEnum status) {
        return ResponseEntity.ok(projectService.changeProjectStatus(principal, projectId, status));
    }

    @GetMapping("/{projectId}/applications")
    public ResponseEntity<List<ApplicationDto>> getAllApplications(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAllApplicationsByProjectId(principal, projectId));
    }

    // Feedback endpoints
    @PostMapping("/{projectId}/feedback")
    public ResponseEntity<FeedbackDto> giveFeedback(Principal principal, @PathVariable Long projectId, @RequestBody FeedbackCreateDto feedbackDto) {
       return ResponseEntity.ok(feedbackService.giveFeedback(principal, projectId, feedbackDto));
    }

    @PutMapping("/{projectId}/feedback/{feedbackId}")
    public ResponseEntity<FeedbackDto> updateFeedback(
            Principal principal,
            @PathVariable Long projectId,
            @PathVariable Long feedbackId,
            @RequestBody FeedbackCreateDto feedbackDto) {
        return ResponseEntity.ok(feedbackService.updateFeedback(principal, projectId, feedbackId, feedbackDto));
    }

    @DeleteMapping("/{projectId}/feedback/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(
            Principal principal,
            @PathVariable Long projectId,
            @PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(principal, projectId, feedbackId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/feedback")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackForProject(principal, projectId));
    }

    @PostMapping("/{projectId}/apply")
    public ResponseEntity<ApplicationDto> applyForProject(Principal principal, @PathVariable Long projectId, @RequestBody ApplicationCreateDto application) {
        return ResponseEntity.ok(applicationService.createApplication(principal, projectId, application));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(Principal principal, @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(principal, projectId, memberId);
        return ResponseEntity.ok().build();
    }
}
