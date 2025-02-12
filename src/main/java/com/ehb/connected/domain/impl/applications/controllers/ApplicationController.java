package com.ehb.connected.domain.impl.applications.controllers;


import com.ehb.connected.domain.impl.applications.service.ApplicationServiceImpl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/applications")
public class ApplicationController {

    private final ApplicationServiceImpl applicationService;

    public ApplicationController(ApplicationServiceImpl applicationService) {
        this.applicationService = applicationService;
    }

}
