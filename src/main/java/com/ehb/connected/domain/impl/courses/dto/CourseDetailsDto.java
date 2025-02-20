package com.ehb.connected.domain.impl.courses.dto;

import com.ehb.connected.domain.impl.assignments.dto.AssignmentDetailsDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CourseDetailsDto {
    private Long id;
    private Long canvasId;
    private String uuid;
    private String name;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long ownerId;
    private List<AssignmentDetailsDto> assignments;
}
