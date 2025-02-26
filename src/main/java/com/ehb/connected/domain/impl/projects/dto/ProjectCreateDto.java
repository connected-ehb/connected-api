package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProjectCreateDto {

    private String title;
    private String description;
    private String shortDescription;

    private Integer teamSize;

    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;

    private List<TagDto> tags;

    public ProjectCreateDto(String title, String description, String shortDescription, String repositoryUrl, String boardUrl, String backgroundImage, int teamSize, Object o) {
    }
}
