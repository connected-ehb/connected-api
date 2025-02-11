package com.ehb.connected.domain.impl.applications.service;

import com.ehb.connected.domain.impl.applications.entities.Application;

import java.util.List;

public interface ApplicationService {
    Application getApplicationById(Long id);
    Application createApplication(Application application);
    Application updateApplication(Application application);
    void deleteApplication(Long id);
    List<Application> findAllApplications(Long id);

}
