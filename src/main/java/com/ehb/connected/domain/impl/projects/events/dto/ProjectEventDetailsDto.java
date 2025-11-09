package com.ehb.connected.domain.impl.projects.events.dto;

import com.ehb.connected.domain.impl.projects.events.entities.ProjectEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProjectEventDetailsDto {
    private ProjectEventType type;
    private String message;
    private String username;
    private LocalDateTime date;
}
