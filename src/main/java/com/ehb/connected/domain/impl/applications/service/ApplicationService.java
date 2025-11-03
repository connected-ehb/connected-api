package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ApplicationService {
    ApplicationDetailsDto getById(Authentication authentication, Long id);
    ApplicationDetailsDto create(Authentication authentication, Long projectId, ApplicationCreateDto application);
    ApplicationDetailsDto reviewApplication(Authentication authentication, Long applicationId, ApplicationStatusEnum status);
    List<ApplicationDetailsDto> getAllApplications(Authentication authentication, Long id);
    ApplicationDetailsDto joinProject(Authentication authentication, Long applicationId);
}
