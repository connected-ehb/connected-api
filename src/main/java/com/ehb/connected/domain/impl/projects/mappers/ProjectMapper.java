package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {
    private final TagMapper tagMapper;
    private final UserDetailsMapper userMapper;
    private final AssignmentRepository assignmentRepository;

    public ProjectMapper(TagMapper tagMapper, UserDetailsMapper userMapper, AssignmentRepository assignmentRepository) {
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.assignmentRepository = assignmentRepository;
    }

    public List<ProjectDetailsDto> toDetailsDtoList(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }
        return projects.stream()
                .map(this::toDetailsDto)
                .collect(Collectors.toList());
    }

    public ProjectDetailsDto toDetailsDto(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus().name());
        dto.setRepositoryUrl(project.getRepositoryUrl());
        dto.setBoardUrl(project.getBoardUrl());
        dto.setBackgroundImage(project.getBackgroundImage());

        if (project.getAssignment() != null) {
            dto.setAssignmentId(project.getAssignment().getId());
        }

        if (project.getTags() != null) {
            dto.setTags(project.getTags().stream().map(tagMapper::toDto).collect(Collectors.toList()));
        } else {
            dto.setTags(List.of());
        }

        dto.setCreatedBy(userMapper.toUserDetailsDto(project.getCreatedBy()));

        if (project.getMembers() != null) {
            dto.setMembers(project.getMembers().stream().map(userMapper::toUserDetailsDto).collect(Collectors.toList()));
        } else {
            dto.setMembers(List.of());
        }

        return dto;
    }

    public Project toEntity(ProjectCreateDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setRepositoryUrl(dto.getRepositoryUrl());
        project.setBoardUrl(dto.getBoardUrl());
        project.setBackgroundImage(dto.getBackgroundImage());
        project.setTags(dto.getTags());

        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        project.setAssignment(assignment);

        return project;
    }

    public void updateEntity(ProjectUpdateDto dto, Project project) {
        if (dto == null || project == null) {
            return;
        }

        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setRepositoryUrl(dto.getRepositoryUrl());
        project.setBoardUrl(dto.getBoardUrl());
        project.setBackgroundImage(dto.getBackgroundImage());
    }
}
