package com.ehb.connected.domain.impl.projects.service;

import com.ehb.connected.domain.impl.applications.dto.ApplicationDto;
import com.ehb.connected.domain.impl.applications.entities.ApplicationStatusEnum;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;

import java.security.Principal;
import java.util.List;

public interface ProjectService {
    List<ProjectDetailsDto> getAllProjects(Long assignmentId);
    ProjectDetailsDto getProjectById(Long id);
    ProjectDetailsDto createProject(Principal principal, Long assignmentId, ProjectCreateDto project);
    ProjectDetailsDto updateProject(Principal principal, Long id, ProjectUpdateDto project);
    void deleteProject(Long id);

    void approveProject(Long id);

    void rejectProject(Long id);

    List<ApplicationDto> getAllApplications(Principal principal, Long id);

    void reviewApplication(Principal principal, Long id, Long applicationId, ApplicationStatusEnum status);

    void removeMember(Principal principal, Long id, Long memberId);
}
