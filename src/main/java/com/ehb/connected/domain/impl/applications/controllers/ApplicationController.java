package com.ehb.connected.domain.impl.applications.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;


    @PreAuthorize("hasAnyAuthority('application:review')")
    @PostMapping("/{applicationId}/review")
    public ResponseEntity<ApplicationDetailsDto> reviewApplication(Principal principal, @PathVariable Long applicationId, @RequestHeader ApplicationStatusEnum status) {
        return ResponseEntity.ok(applicationService.reviewApplication(principal, applicationId, status));
    }

    @PreAuthorize("hasAnyAuthority('application:join')")
    @PostMapping("/{applicationId}/join")
    public ResponseEntity<ApplicationDetailsDto> joinProjectByApplication(Principal principal, @PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.joinProject(principal, applicationId));
    }

    @PreAuthorize("hasAnyAuthority('application:read')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailsDto> getApplicationById(Principal principal, @PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.getById(principal, applicationId));
    }
}
