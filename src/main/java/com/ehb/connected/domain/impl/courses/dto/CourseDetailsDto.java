package com.ehb.connected.domain.impl.courses.dto;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseDetailsDto {
    Long id;
    String uuid;
    String name;
    LocalDateTime startAt;
    LocalDateTime endAt;
    Long ownerId;
    Long canvasCourseId;
    AssignmentDetailsDto[] assignments;
}
