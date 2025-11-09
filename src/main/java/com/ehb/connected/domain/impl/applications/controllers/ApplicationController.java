package com.ehb.connected.domain.impl.applications.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;


    @PreAuthorize("hasAnyAuthority('application:review')")
    @PostMapping("/{applicationId}/review")
    public ResponseEntity<ApplicationDetailsDto> reviewApplication(Authentication authentication, @PathVariable Long applicationId, @RequestHeader ApplicationStatusEnum status) {
        return ResponseEntity.ok(applicationService.reviewApplication(authentication, applicationId, status));
    }

    @PreAuthorize("hasAnyAuthority('application:join')")
    @PostMapping("/{applicationId}/join")
    public ResponseEntity<ApplicationDetailsDto> joinProjectByApplication(Authentication authentication, @PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.joinProject(authentication, applicationId));
    }

    @PreAuthorize("hasAnyAuthority('application:read')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<ApplicationDetailsDto> getApplicationById(Authentication authentication, @PathVariable Long applicationId) {
        return ResponseEntity.ok(applicationService.getById(authentication, applicationId));
    }
}
