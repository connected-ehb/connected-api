package com.ehb.connected.domain.impl.projects.events.mappers;

import com.ehb.connected.domain.impl.projects.events.dto.ProjectEventDetailsDto;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectEventMapper {

    public ProjectEventDetailsDto toDetailsDto(ProjectEvent event) {
        ProjectEventDetailsDto dto = new ProjectEventDetailsDto();
        dto.setType(event.getType());
        dto.setMessage(event.getMessage());
        dto.setDate(event.getTimestamp());
        dto.setUsername(event.getActor() != null ? event.getActor().getFullName() : "System");
        return dto;
    }
}
