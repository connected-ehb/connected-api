package com.ehb.connected.domain.impl.projects.mappers;

import com.ehb.connected.domain.impl.projects.dto.ProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectCreateDto;
import com.ehb.connected.domain.impl.projects.dto.ProjectUpdateDto;
import com.ehb.connected.domain.impl.projects.dto.ResearcherProjectDetailsDto;
import com.ehb.connected.domain.impl.projects.entities.Project;
import com.ehb.connected.domain.impl.projects.service.ProjectUserService;
import com.ehb.connected.domain.impl.tags.mappers.TagMapper;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectMapper {
    private final TagMapper tagMapper;
    private final UserDetailsMapper userMapper;
    private final ProjectUserService projectUserService;


    public List<ProjectDetailsDto> toDetailsDtoList(List<Project> projects) {
        return projects.stream().map(this::toDetailsDto).toList();
    }

    public List<ResearcherProjectDetailsDto> toResearcherDetailsDtoList(List<Project> projects) {
        return projects.stream().map(this::toResearcherDetailsDto).toList();
    }

    public ProjectDetailsDto toDetailsDto(Project project) {
        return new ProjectDetailsDto(
                project.getId(),
                project.getGid(),
                project.getTitle(),
                project.getDescription(),
                project.getShortDescription(),
                project.getStatus(),
                project.getRepositoryUrl(),
                project.getBoardUrl(),
                project.getBackgroundImage(),
                project.getTeamSize(),
                project.getAssignment() != null ? project.getAssignment().getId() : null,
                project.getTags() != null ? project.getTags().stream().map(tagMapper::toDto).toList() : Collections.emptyList(),
                project.getCreatedBy() != null ? userMapper.toUserDetailsDto(project.getCreatedBy()) : null,
                project.getProductOwner() != null ? userMapper.toUserDetailsDto(project.getProductOwner()) : null,
                project.getMembers() != null ? project.getMembers().stream().map(userMapper::toUserDetailsDto).toList() : Collections.emptyList()
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
        if (dto.getTags() != null) {
            project.setTags(tagMapper.toEntityList(dto.getTags()));
        }
        return project;
    }

    public void updateEntityFromDto(User user, ProjectUpdateDto dto, Project project) {
        if(project.isEditable(user)) {
            project.setTitle(dto.getTitle());
            project.setDescription(dto.getDescription());
            project.setShortDescription(dto.getShortDescription());
            project.setTeamSize(dto.getTeamSize());
        }

        project.setRepositoryUrl(dto.getRepositoryUrl());
        project.setBoardUrl(dto.getBoardUrl());
        project.setBackgroundImage(dto.getBackgroundImage());

        project.getTags().clear();
        project.getTags().addAll(tagMapper.toEntityList(dto.getTags()));
    }

    public ResearcherProjectDetailsDto toResearcherDetailsDto(Project project) {
        ResearcherProjectDetailsDto dto = new ResearcherProjectDetailsDto();

        dto.setId(project.getId());
        dto.setGid(project.getGid());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setShortDescription(project.getShortDescription());
        dto.setStatus(project.getStatus());
        dto.setRepositoryUrl(project.getRepositoryUrl());
        dto.setBoardUrl(project.getBoardUrl());
        dto.setBackgroundImage(project.getBackgroundImage());
        dto.setTeamSize(project.getTeamSize());
        dto.setAssignmentId(project.getAssignment() != null ? project.getAssignment().getId() : null);
        dto.setTags(project.getTags() != null ? project.getTags().stream().map(tagMapper::toDto).toList() : Collections.emptyList());
        dto.setCreatedBy(project.getCreatedBy() != null ? userMapper.toUserDetailsDto(project.getCreatedBy()) : null);
        dto.setProductOwner(project.getProductOwner() != null ? userMapper.toUserDetailsDto(project.getProductOwner()) : null);
        dto.setMembers(project.getMembers() != null ? project.getMembers().stream().map(userMapper::toUserDetailsDto).toList() : Collections.emptyList());

        if (project.getAssignment() != null) {
            dto.setCourseName(project.getAssignment().getCourse().getName());
            dto.setAssignmentName(project.getAssignment().getName());
        }
        return dto;
    }
}
