package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@RequiredArgsConstructor
public class ProjectMapper {
    private final TagMapper tagMapper;
    private final UserDetailsMapper userMapper;

    public List<ProjectDetailsDto> toDetailsDtoList(List<Project> projects) {
        return projects.stream().map(this::toDetailsDto).toList();
    }

    public ProjectDetailsDto toDetailsDto(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDetailsDto dto = new ProjectDetailsDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setShortDescription(project.getShortDescription());
        dto.setStatus(project.getStatus().name());
        dto.setRepositoryUrl(project.getRepositoryUrl());
        dto.setBoardUrl(project.getBoardUrl());
        dto.setBackgroundImage(project.getBackgroundImage());

        if (project.getAssignment() != null) {
            dto.setAssignmentId(project.getAssignment().getId());
        }

        if (project.getTags() != null) {
            dto.setTags(project.getTags().stream().map(tagMapper::toDto).collect(toList()));
        } else {
            dto.setTags(List.of());
        }

        dto.setCreatedBy(userMapper.toUserDetailsDto(project.getCreatedBy()));

        if (project.getMembers() != null) {
            dto.setMembers(project.getMembers().stream().map(userMapper::toUserDetailsDto).collect(toList()));
        } else {
            dto.setMembers(List.of());
        }

        return dto;
    }

    public Project toEntity(ProjectCreateDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setShortDescription(dto.getShortDescription());
        project.setRepositoryUrl(dto.getRepositoryUrl());
        project.setBoardUrl(dto.getBoardUrl());
        project.setBackgroundImage(dto.getBackgroundImage());
        project.setTags(dto.getTags());

        return project;
    }
}
