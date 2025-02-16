package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.tags.entities.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreateDto {

    private Long assignmentId;

    private String title;
    private String description;
    private String shortDescription;

    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private List<Tag> tags;
}
