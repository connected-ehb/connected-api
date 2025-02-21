package com.ehb.connected.domain.impl.projects.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
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

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> getProjectById(Principal principal, @PathVariable Long projectId){
        return ResponseEntity.ok(projectService.getProjectById(principal, projectId));
    }

    //TODO: find better endpoint
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<ProjectDetailsDto>> getAllProjects(@PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllProjectsByAssignmentId(assignmentId));
    }

    @GetMapping("/{assignmentId}/published")
    public ResponseEntity<List<ProjectDetailsDto>> getAllPublishedProjects(Principal principal, @PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllPublishedOrOwnedProjectsByAssignmentId(principal, assignmentId));
    }

    @PostMapping("/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> createProject(Principal principal, @PathVariable Long assignmentId, @RequestBody ProjectCreateDto project){
        return ResponseEntity.ok(projectService.createProject(principal, assignmentId, project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> updateProject(Principal principal, @PathVariable Long projectId, @RequestBody ProjectUpdateDto project){
        return ResponseEntity.ok(projectService.updateProject(principal, projectId, project));
    }

    @PostMapping("/{projectId}/status")
    public ResponseEntity<ProjectDetailsDto> changeProjectStatus(Principal principal, @PathVariable Long projectId, @RequestHeader ProjectStatusEnum status) {
        return ResponseEntity.ok(projectService.changeProjectStatus(principal, projectId, status));
    }

    @GetMapping("/{projectId}/applications")
    public ResponseEntity<List<ApplicationDetailsDto>> getAllApplications(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAllApplicationsByProjectId(principal, projectId));
    }

    // Feedback endpoints
    @PostMapping("/{projectId}/feedback")
    public ResponseEntity<FeedbackDto> giveFeedback(Principal principal, @PathVariable Long projectId, @RequestBody FeedbackCreateDto feedbackDto) {
       return ResponseEntity.ok(feedbackService.giveFeedback(principal, projectId, feedbackDto));
    }



    @GetMapping("/{projectId}/feedback")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackForProject(principal, projectId));
    }

    @PostMapping("/{projectId}/apply")
    public ResponseEntity<ApplicationDetailsDto> applyForProject(Principal principal, @PathVariable Long projectId, @RequestBody ApplicationCreateDto application) {
        return ResponseEntity.ok(applicationService.createApplication(principal, projectId, application));
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(Principal principal, @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(principal, projectId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{assignmentId}/publish")
    public ResponseEntity<List<ProjectDetailsDto>> publishAllProjects(Principal principal, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(projectService.publishAllProjects(principal, assignmentId));
    }
}
