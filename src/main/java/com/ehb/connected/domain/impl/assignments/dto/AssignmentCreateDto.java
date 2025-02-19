package com.ehb.connected.domain.impl.assignments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentCreateDto {

     private String name;
     private String description;
     private Integer defaultTeamSize;
     private Long canvasAssignmentId;
     private Long courseId;
}
