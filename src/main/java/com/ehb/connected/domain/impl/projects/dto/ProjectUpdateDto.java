package com.ehb.connected.domain.impl.projects.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectUpdateDto {
    private String title;

    private String description;
    private String shortDescription;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private List<Long> tagIds;
}
