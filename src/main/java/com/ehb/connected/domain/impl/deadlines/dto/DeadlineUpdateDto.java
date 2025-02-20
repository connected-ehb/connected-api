package com.ehb.connected.domain.impl.deadlines.dto;

import com.ehb.connected.domain.impl.deadlines.enums.DeadlineRestriction;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeadlineUpdateDto {
    private String title;
    private LocalDateTime dueDate;
    private String description;
    private DeadlineRestriction restriction;
}
