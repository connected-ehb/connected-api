package com.ehb.connected.domain.impl.assignments.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignmentDetailsDto {

    Long id;
    String name;
    String description;
    Integer defaultTeamSize;
    Long canvasAssignmentId;
    Long courseId;
}
