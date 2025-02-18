package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProjectMapper {
    private final TagMapper tagMapper;
    private final UserDetailsMapper userMapper;


    public List<ProjectDetailsDto> toDetailsDtoList(List<Project> projects) {
        return projects.stream().map(this::toDetailsDto).toList();
    }

    public ProjectDetailsDto toDetailsDto(Project project) {
        return new ProjectDetailsDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getShortDescription(),
                project.getStatus(),
                project.getRepositoryUrl(),
                project.getBoardUrl(),
                project.getBackgroundImage(),
                project.getAssignment() != null ? project.getAssignment().getId() : null,
                project.getTags() != null ? project.getTags().stream().map(tagMapper::toDto).collect(Collectors.toList()) : Collections.emptyList(),
                userMapper.toUserDetailsDto(project.getCreatedBy()),
                project.getMembers() != null ? project.getMembers().stream().map(userMapper::toUserDetailsDto).collect(Collectors.toList()) : Collections.emptyList()
        );
    }

    public Project toEntity(ProjectCreateDto dto) {
        Project project = new Project();
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDescription());
        project.setShortDescription(dto.getShortDescription());
        project.setRepositoryUrl(dto.getRepositoryUrl());
        project.setBoardUrl(dto.getBoardUrl());
        project.setBackgroundImage(dto.getBackgroundImage());
        project.setTags(tagMapper.toEntityList(dto.getTags()));

        return project;
    }
}
