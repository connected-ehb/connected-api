package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;

import java.security.Principal;
import java.util.List;

public interface ApplicationService {
    Application getApplicationById(Long id);
    ApplicationDto createApplication(Principal principal, Long projectId, ApplicationCreateDto application);
    Application updateApplication(Application application);
    void deleteApplication(Long id);
    List<Application> findAllApplications(Long id);

}
