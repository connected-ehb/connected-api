package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDetailsDto {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private String status;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private Long assignmentId;
    private List<TagDto> tags;
    private UserDetailsDto createdBy;
    private List<UserDetailsDto> members;
}
