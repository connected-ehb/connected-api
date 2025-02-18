package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;

import java.security.Principal;
import java.util.List;

public interface ProjectService {
    List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId);
    List<ProjectDetailsDto> getAllPublishedOrOwnedProjectsByAssignmentId(Principal principal, Long assignmentId);
    ProjectDetailsDto getProjectById(Long id);
    List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status);
    ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto project);
    ProjectDetailsDto updateProject(Principal principal, Long id, ProjectUpdateDto project);

    ProjectDetailsDto changeProjectStatus(Principal principal, Long id, ProjectStatusEnum status);

    List<ApplicationDto> getAllApplicationsByProjectId(Principal principal, Long projectId);

    void removeMember(Principal principal, Long id, Long memberId);
}
