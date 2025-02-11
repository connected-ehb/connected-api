package com.ehb.connected.domain.impl.courses.dto;

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
}
