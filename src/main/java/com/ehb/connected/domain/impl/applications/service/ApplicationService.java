package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;

import java.security.Principal;
import java.util.List;

public interface ApplicationService {
    ApplicationDetailsDto getById(Principal principal, Long id);
    ApplicationDetailsDto create(Principal principal, Long projectId, ApplicationCreateDto application);
    ApplicationDetailsDto reviewApplication(Principal principal, Long applicationId, ApplicationStatusEnum status);
    List<ApplicationDetailsDto> getAllApplications(Principal principal, Long id);
    ApplicationDetailsDto joinProject(Principal principal, Long applicationId);
}
