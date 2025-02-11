package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.entities.Application;
import com.ehb.connected.domain.impl.projects.entities.Project;

import java.security.Principal;
import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();
    Project getProjectById(Long id);
    Project createProject(Project project);
    Project updateProject(Long id, Project project);
    void deleteProject(Long id);

    void approveProject(Long id);

    void rejectProject(Long id);

    List<Application> getAllApplications(Principal principal, Long id);

    void reviewApplication(Principal principal, Long id, Long applicationId, String status);
}
