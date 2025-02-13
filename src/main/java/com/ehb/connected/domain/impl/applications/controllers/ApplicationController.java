package com.ehb.connected.domain.impl.applications.controllers;


import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.mappers.ApplicationMapper;
import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/applications")
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;
    private final ApplicationMapper applicationMapper;

    public ApplicationController(ApplicationServiceImpl applicationService, ApplicationMapper applicationMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
    }



    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> getApplicationById(@PathVariable Long id) {
        ApplicationDto applicationDto = applicationMapper.toDto(applicationService.getApplicationById(id));
        return ResponseEntity.ok(applicationDto);
    }




}
