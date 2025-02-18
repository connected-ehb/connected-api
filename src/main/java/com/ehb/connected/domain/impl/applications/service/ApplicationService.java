package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationCreateDto;
import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;

import java.security.Principal;
import java.util.List;

public interface ApplicationService {
    Application getApplicationById(Long id);
    ApplicationDto createApplication(Principal principal, Long projectId, ApplicationCreateDto application);
    void reviewApplication(Principal principal, Long applicationId, ApplicationStatusEnum status);
    List<Application> findAllApplications(Long id);

    List<Application> findAllApplicationsByUserAndAssignment(Long id, Long assignmentId);
}
