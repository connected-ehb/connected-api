package com.ehb.connected.domain.impl.assignments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentDetailsDto {

    private Long id;
    private Long canvasId;
    private String name;
    private String description;
    private Integer defaultTeamSize;
    private Long courseId;
}
