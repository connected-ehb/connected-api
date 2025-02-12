package com.ehb.connected.domain.impl.courses.dto;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    List<AssignmentDetailsDto> assignments;
}
