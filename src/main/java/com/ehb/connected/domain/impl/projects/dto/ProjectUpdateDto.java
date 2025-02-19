package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectUpdateDto {
    private String title;

    private String description;
    private String shortDescription;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private List<TagDto> tags;
}
