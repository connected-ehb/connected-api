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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> getProjectById(Principal principal, @PathVariable Long projectId){
        return ResponseEntity.ok(projectService.getProjectById(principal, projectId));
    }

    //TODO: find better endpoint
    @PreAuthorize("hasAnyAuthority('project:read_all')")
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<ProjectDetailsDto>> getAllProjects(@PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllProjectsByAssignmentId(assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:read_published_or_owned')")
    @GetMapping("/{assignmentId}/published")
    public ResponseEntity<List<ProjectDetailsDto>> getAllPublishedProjects(Principal principal, @PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllPublishedOrOwnedProjectsByAssignmentId(principal, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:create')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> createProject(Principal principal, @PathVariable Long assignmentId, @RequestBody ProjectCreateDto project){
        return ResponseEntity.ok(projectService.createProject(principal, assignmentId, project));
    }

    @PreAuthorize("hasAnyAuthority('project:update')")
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> updateProject(Principal principal, @PathVariable Long projectId, @RequestBody ProjectUpdateDto project){
        return ResponseEntity.ok(projectService.updateProject(principal, projectId, project));
    }

    @PreAuthorize("hasAnyAuthority('project:change_status')")
    @PostMapping("/{projectId}/status")
    public ResponseEntity<ProjectDetailsDto> changeProjectStatus(Principal principal, @PathVariable Long projectId, @RequestHeader ProjectStatusEnum status) {
        return ResponseEntity.ok(projectService.changeProjectStatus(principal, projectId, status));
    }

    @PreAuthorize("hasAnyAuthority('application:read')")
    @GetMapping("/{projectId}/applications")
    public ResponseEntity<List<ApplicationDetailsDto>> getAllApplications(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAllApplicationsByProjectId(principal, projectId));
    }

    // Feedback endpoints
    @PreAuthorize("hasAnyAuthority('feedback:create')")
    @PostMapping("/{projectId}/feedback")
    public ResponseEntity<FeedbackDto> giveFeedback(Principal principal, @PathVariable Long projectId, @RequestBody FeedbackCreateDto feedbackDto) {
       return ResponseEntity.ok(feedbackService.giveFeedback(principal, projectId, feedbackDto));
    }

    @PreAuthorize("hasAnyAuthority('feedback:read')")
    @GetMapping("/{projectId}/feedback")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks(Principal principal, @PathVariable Long projectId) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackForProject(principal, projectId));
    }


    @PreAuthorize("hasAnyAuthority('project:apply')")
    @PostMapping("/{projectId}/apply")
    public ResponseEntity<ApplicationDetailsDto> applyForProject(Principal principal, @PathVariable Long projectId, @RequestBody ApplicationCreateDto application) {
        return ResponseEntity.ok(applicationService.createApplication(principal, projectId, application));
    }

    @PreAuthorize("hasAnyAuthority('project:remove_member')")
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(Principal principal, @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(principal, projectId, memberId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('project:publish')")
    @PostMapping("/{assignmentId}/publish")
    public ResponseEntity<List<ProjectDetailsDto>> publishAllProjects(Principal principal, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(projectService.publishAllProjects(principal, assignmentId));
    }
}
