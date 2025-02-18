package com.ehb.connected.domain.impl.applications.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;

    @PostMapping("/{applicationId}/review")
    public ResponseEntity<ApplicationDetailsDto> reviewApplication(Principal principal, @PathVariable Long applicationId, @RequestHeader ApplicationStatusEnum status) {
        applicationService.reviewApplication(principal, applicationId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDetailsDto> getApplicationById(@PathVariable Long id) {
        ApplicationDetailsDto applicationDetailsDto = applicationMapper.toDto(applicationService.getApplicationById(id));
        return ResponseEntity.ok(applicationDetailsDto);
    }
}
