package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.projects.entities.Project;

import java.util.List;

public interface ProjectService {
    List<Project> getAllProjects();
    Project getProjectById(Long id);
    Project createProject(Project project);
    Project updateProject(Project project);
    void deleteProject(Long id);

}
