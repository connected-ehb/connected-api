package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectService {
    ProjectDetailsDto getProjectById(Authentication authentication, Long id);
    Project getProjectById(Long id);
    List<ProjectDetailsDto> getAllProjectsByAssignmentId(Long assignmentId);
    ProjectDetailsDto getProjectByUserAndAssignmentId(Authentication authentication, Long assignmentId);
    List<ProjectDetailsDto> getAllPublishedOrOwnedProjectsByAssignmentId(Authentication authentication, Long assignmentId);
    List<Project> getAllProjectsByStatus(Long assignmentId, ProjectStatusEnum status);
    ProjectDetailsDto createProject(Authentication authentication, Long assignmentId, ProjectCreateDto project);
    ProjectDetailsDto save(Authentication authentication, Long id, ProjectUpdateDto project);

    List<ProjectDetailsDto> findAllInAssignmentCreatedBy(Long assignmentId, Authentication authentication);

    void save(Project project);

    ProjectDetailsDto changeProjectStatus(Authentication authentication, Long id, ProjectStatusEnum status);
    List<ProjectDetailsDto> publishAllProjects(Authentication authentication, Long assignmentId);

    List<ApplicationDetailsDto> getAllApplicationsByProjectId(Authentication authentication, Long projectId);

    void removeMember(Authentication authentication, Long id, Long memberId);

    ProjectDetailsDto claimProject(Authentication authentication, Long projectId);

    ProjectDetailsDto importProject(Authentication authentication, Long assignmentId, Long projectId);

    ProjectDetailsDto createGlobalProject(Authentication authentication, ProjectCreateDto project);

    List<ProjectDetailsDto> getAllGlobalProjects(Authentication authentication);

    void leaveProject(Authentication authentication, Long projectId);
}
