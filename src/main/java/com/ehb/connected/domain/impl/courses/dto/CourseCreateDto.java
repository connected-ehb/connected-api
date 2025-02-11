package com.ehb.connected.domain.impl.courses.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseCreateDto {
    String id;
    String uuid;
    String name;
    LocalDateTime start_at;
    LocalDateTime end_at;

}
