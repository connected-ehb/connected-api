package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
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
                project.getTeamSize(),
                project.getAssignment() != null ? project.getAssignment().getId() : null,
                project.getTags() != null ? project.getTags().stream().map(tagMapper::toDto).collect(Collectors.toList()) : Collections.emptyList(),
                project.getCreatedBy() != null ? userMapper.toUserDetailsDto(project.getCreatedBy()) : null,
                project.getProductOwner() != null ? userMapper.toUserDetailsDto(project.getProductOwner()) : null,
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
        project.setTeamSize(dto.getTeamSize());
        project.setBackgroundImage(dto.getBackgroundImage());
        project.setTags(tagMapper.toEntityList(dto.getTags()));

        return project;
    }

    public void updateEntityFromDto(ProjectUpdateDto dto, Project entity) {
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getShortDescription() != null) {
            entity.setShortDescription(dto.getShortDescription());
        }
        if (dto.getRepositoryUrl() != null) {
            entity.setRepositoryUrl(dto.getRepositoryUrl());
        }
        if (dto.getBoardUrl() != null) {
            entity.setBoardUrl(dto.getBoardUrl());
        }
        if (dto.getBackgroundImage() != null) {
            entity.setBackgroundImage(dto.getBackgroundImage());
        }
        if (dto.getTeamSize() != 0) {
            entity.setTeamSize(dto.getTeamSize());
        }
        if (dto.getTags() != null) {
            entity.getTags().clear();
            entity.getTags().addAll(tagMapper.toEntityList(dto.getTags()));
        }
    }
}
