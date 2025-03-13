package com.ehb.connected.domain.impl.courses.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CourseCreateDto {
    private Long canvasId;
    private String uuid;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
