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
import com.ehb.connected.domain.impl.projects.dto.ResearcherProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.projects.events.dto.ProjectEventDetailsDto;
import com.ehb.connected.domain.impl.projects.events.service.ProjectEventService;
import com.ehb.connected.domain.impl.projects.service.ProjectService;
import com.ehb.connected.domain.impl.reviews.dto.ReviewCreateDto;
import com.ehb.connected.domain.impl.reviews.dto.ReviewDetailsDto;
import com.ehb.connected.domain.impl.reviews.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final FeedbackService feedbackService;
    private final ApplicationService applicationService;
    private final ReviewService reviewService;
    private final ProjectEventService projectEventService;

    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> getProjectById(Authentication authentication, @PathVariable Long projectId){
        return ResponseEntity.ok(projectService.getProjectById(authentication, projectId));
    }

    //TODO: find better endpoint
    @PreAuthorize("hasAnyAuthority('project:read_all')")
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<ProjectDetailsDto>> getAllProjects(@PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllProjectsByAssignmentId(assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/member/assignment/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> getProjectByUserAndAssignmentId(Authentication authentication, @PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getProjectByUserAndAssignmentId(authentication, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:read_published_or_owned')")
    @GetMapping("/{assignmentId}/published")
    public ResponseEntity<List<ProjectDetailsDto>> getAllPublishedProjects(Authentication authentication, @PathVariable Long assignmentId){
        return ResponseEntity.ok(projectService.getAllPublishedOrOwnedProjectsByAssignmentId(authentication, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:create')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> createProject(Authentication authentication, @PathVariable Long assignmentId, @RequestBody ProjectCreateDto project){
        return ResponseEntity.ok(projectService.createProject(authentication, assignmentId, project));
    }

    @PreAuthorize("hasAnyAuthority('project:create_global')")
    @PostMapping("/global")
    public ResponseEntity<ProjectDetailsDto> createGlobalProject(Authentication authentication, @RequestBody ProjectCreateDto project){
        return ResponseEntity.ok(projectService.createGlobalProject(authentication, project));
    }

    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/global")
    public ResponseEntity<List<ProjectDetailsDto>> getAllGlobalProjects(Authentication authentication){
        return ResponseEntity.ok(projectService.getAllGlobalProjects(authentication));
    }

    @PreAuthorize("hasAnyAuthority('project:read_imported')")
    @GetMapping("/global/imported")
    public ResponseEntity<List<ResearcherProjectDetailsDto>> getAllImportedProjects(Authentication authentication) {
        return ResponseEntity.ok(projectService.getAllImportedProjects(authentication));
    }

    @PreAuthorize("hasAnyAuthority('project:update')")
    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectDetailsDto> updateProject(Authentication authentication, @PathVariable Long projectId, @Valid @RequestBody ProjectUpdateDto project){
        return ResponseEntity.ok(projectService.save(authentication, projectId, project));
    }

    @PreAuthorize("hasAnyAuthority('project:change_status')")
    @PostMapping("/{projectId}/status")
    public ResponseEntity<ProjectDetailsDto> changeProjectStatus(Authentication authentication, @PathVariable Long projectId, @RequestHeader ProjectStatusEnum status) {
        return ResponseEntity.ok(projectService.changeProjectStatus(authentication, projectId, status));
    }

    @PreAuthorize("hasAnyAuthority('application:read')")
    @GetMapping("/{projectId}/applications")
    public ResponseEntity<List<ApplicationDetailsDto>> getAllApplications(Authentication authentication, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getAllApplicationsByProjectId(authentication, projectId));
    }

    // Feedback endpoints
    @PreAuthorize("hasAnyAuthority('feedback:create')")
    @PostMapping("/{projectId}/feedback")
    public ResponseEntity<FeedbackDto> giveFeedback(Authentication authentication, @PathVariable Long projectId, @RequestBody FeedbackCreateDto feedbackDto) {
       return ResponseEntity.ok(feedbackService.giveFeedback(authentication, projectId, feedbackDto));
    }

    @PreAuthorize("hasAnyAuthority('feedback:read')")
    @GetMapping("/{projectId}/feedback")
    public ResponseEntity<List<FeedbackDto>> getFeedbacks(Authentication authentication, @PathVariable Long projectId) {
        return ResponseEntity.ok(feedbackService.getAllFeedbackForProject(authentication, projectId));
    }


    @PreAuthorize("hasAnyAuthority('project:apply')")
    @PostMapping("/{projectId}/apply")
    public ResponseEntity<ApplicationDetailsDto> applyForProject(Authentication authentication, @PathVariable Long projectId, @RequestBody ApplicationCreateDto application) {
        return ResponseEntity.ok(applicationService.create(authentication, projectId, application));
    }

    @PreAuthorize("hasAnyAuthority('project:claim')")
    @PostMapping("/{projectId}/claim")
    public ResponseEntity<ProjectDetailsDto> claimProject(Authentication authentication, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.claimProject(authentication, projectId));
    }

    @PreAuthorize("hasAnyAuthority('project:remove_member')")
    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(Authentication authentication, @PathVariable Long projectId, @PathVariable Long memberId) {
        projectService.removeMember(authentication, projectId, memberId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyAuthority('project:publish')")
    @PostMapping("/{assignmentId}/publish")
    public ResponseEntity<List<ProjectDetailsDto>> publishAllProjects(Authentication authentication, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(projectService.publishAllProjects(authentication, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('project:import')")
    @PostMapping("/{projectId}/import/{assignmentId}")
    public ResponseEntity<ProjectDetailsDto> importProject(Authentication authentication, @PathVariable Long assignmentId, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.importProject(authentication, assignmentId, projectId));
    }

    @PreAuthorize("hasAnyAuthority('review:read_all')")
    @GetMapping("{projectId}/reviews")
    public ResponseEntity<List<ReviewDetailsDto>> getAllReviews(Authentication authentication, @PathVariable Long projectId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByProjectId(authentication, projectId));
    }

    @PreAuthorize("hasAnyAuthority('review:create')")
    @PostMapping("{projectId}/reviews")
    public ResponseEntity<ReviewDetailsDto> createOrUpdateReviewForProject(Authentication authentication, @PathVariable Long projectId, @RequestBody ReviewCreateDto reviewCreateDto) {
        return ResponseEntity.ok(reviewService.createOrUpdateReviewForProject(authentication, projectId, reviewCreateDto));
    }

    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/my-projects/{assignmentId}")
    public ResponseEntity<List<ProjectDetailsDto>> getMyProjects(Authentication authentication, @PathVariable  Long assignmentId) {
        return ResponseEntity.ok(projectService.findAllInAssignmentCreatedBy(assignmentId, authentication));
    }

    @PreAuthorize("hasAnyAuthority('project:leave')")
    @DeleteMapping("/{projectId}/leave")
    public ResponseEntity<ProjectDetailsDto> leaveProject(Authentication authentication, @PathVariable Long projectId) {
        projectService.leaveProject(authentication, projectId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('event:read')")
    @GetMapping("/{projectId}/events")
    public ResponseEntity<List<ProjectEventDetailsDto>> getEvents(Authentication authentication, @PathVariable Long projectId) {
        return ResponseEntity.ok(projectEventService.getEventsForProject(authentication, projectId));
    }


    @PreAuthorize("hasAnyAuthority('project:read')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProjectDetailsDto>> getProjectsForUser(Authentication authentication, @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getAllProjectsForUser(authentication, userId));
    }
}
