package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;

import java.security.Principal;
import java.util.List;

public interface ProjectService {
    ProjectDetailsDto getProjectById(Principal principal, Long id);
    Project getProjectById(Long id);
    List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId);
    List<ProjectDetailsDto> getAllPublishedOrOwnedProjectsByAssignmentId(Principal principal, Long assignmentId);
    List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status);
    ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto project);
    ProjectDetailsDto updateProject(Principal principal, Long id, ProjectUpdateDto project);

    void updateProject(Project project);

    ProjectDetailsDto changeProjectStatus(Principal principal, Long id, ProjectStatusEnum status);
    List<ProjectDetailsDto> publishAllProjects(Principal principal, Long assignmentId);

    List<ApplicationDetailsDto> getAllApplicationsByProjectId(Principal principal, Long projectId);

    void removeMember(Principal principal, Long id, Long memberId);

    ProjectDetailsDto claimProject(Principal principal, Long projectId);
}
