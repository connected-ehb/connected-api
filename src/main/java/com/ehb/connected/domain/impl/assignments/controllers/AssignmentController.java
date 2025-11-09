package com.ehb.connected.domain.impl.assignments.controllers;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementCreateDto;
import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import com.ehb.connected.domain.impl.announcements.service.AnnouncementService;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.service.ApplicationService;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentCreateDto;
import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.dto.DashboardDetailsDto;
import com.ehb.connected.domain.impl.assignments.service.AssignmentServiceImpl;
import com.ehb.connected.domain.impl.dashboard.service.DashboardServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentServiceImpl assignmentService;
    private final ApplicationService applicationService;
    private final AnnouncementService announcementService;
    private final DashboardServiceImpl dashboardServiceImpl;

    @PreAuthorize("hasAnyAuthority('canvas:sync')")
    @PostMapping("/canvas/{courseId}")
    public ResponseEntity<List<AssignmentDetailsDto>> getAssignmentsFromCanvas(Authentication authentication, @PathVariable Long courseId) {
        List<AssignmentDetailsDto> filteredAssignmentsJson = assignmentService.getNewAssignmentsFromCanvas(authentication, courseId);
        return ResponseEntity.ok(filteredAssignmentsJson);
    }

    @PreAuthorize("hasAnyAuthority('assignment:create')")
    @PostMapping("/")
    public ResponseEntity<AssignmentDetailsDto> createAssignment(@RequestBody AssignmentCreateDto assignmentDto) {
        return ResponseEntity.ok(assignmentService.createAssignment(assignmentDto));
    }

    @PreAuthorize("hasAnyAuthority('assignment:delete')")
    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(Authentication authentication, @PathVariable Long assignmentId) {
        assignmentService.deleteAssignmentById(authentication, assignmentId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyAuthority('application:read_all')")
    @GetMapping("/{assignmentId}/applications")
    public ResponseEntity<List<ApplicationDetailsDto>> getAllApplications(Authentication authentication, @PathVariable Long assignmentId){
        return ResponseEntity.ok(applicationService.getAllApplications(authentication, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('announcement:create')")
    @PostMapping("/{assignmentId}/announcements")
    public ResponseEntity<AnnouncementDetailsDto> createAnnouncement(Authentication authentication, @PathVariable Long assignmentId, @RequestBody AnnouncementCreateDto announcement) {
        return ResponseEntity.ok(announcementService.createAnnouncementByAssignment(authentication, assignmentId, announcement));
    }

    @PreAuthorize("hasAnyAuthority('announcement:read_all')")
    @GetMapping("/{assignmentId}/announcements")
    public ResponseEntity<List<AnnouncementDetailsDto>> getAnnouncements(Authentication authentication, @PathVariable Long assignmentId) {
        return ResponseEntity.ok(announcementService.getAnnouncementsByAssignment(authentication, assignmentId));
    }

    @PreAuthorize("hasAnyAuthority('dashboard:read')")
    @GetMapping("/{assignmentId}/dashboard")
    public ResponseEntity<DashboardDetailsDto> getDashboard(
            @PathVariable Long assignmentId
    ) {
        DashboardDetailsDto dashboard = dashboardServiceImpl.getDashboardDetails(assignmentId);
        return ResponseEntity.ok(dashboard);
    }
}
