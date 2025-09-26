package com.ehb.connected.domain.impl.projects.dto;

import com.ehb.connected.domain.impl.tags.dto.TagDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectUpdateDto {
    @NotNull
    private String title;
    @NotNull
    private String description;
    @NotNull
    private String shortDescription;
    private String repositoryUrl;
    private String boardUrl;
    private String backgroundImage;
    @Min(value = 1, message = "Team size must be at least 1")
    private int teamSize;
    @NotNull
    private List<TagDto> tags;
}
