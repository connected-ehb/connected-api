package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.projects.entities.ProjectStatusEnum;
import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDetailsDto {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private ProjectStatusEnum status;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private Long assignmentId;
    private List<TagDto> tags;
    private UserDetailsDto createdBy;
    private List<UserDetailsDto> members;
}
