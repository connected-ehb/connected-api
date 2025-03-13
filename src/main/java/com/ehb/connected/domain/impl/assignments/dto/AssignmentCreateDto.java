package com.ehb.connected.domain.impl.assignments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentCreateDto {

     private String name;
     private Long canvasId;
     private String description;
     private Integer defaultTeamSize;
     private Long courseId;
}
