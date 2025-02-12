package com.ehb.connected.domain.impl.projects.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectCreateDto {
    private Long assignmentId;

    private String title;

    private String description;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private List<Long> tagIds;
}
