package com.ehb.connected.domain.impl.projects.events.service;

import com.ehb.connected.domain.impl.projects.events.dto.ProjectEventDetailsDto;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEventType;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ProjectEventService {
    List<ProjectEventDetailsDto> getEventsForProject(Authentication authentication, Long projectId);
    void logEvent(Long projectId, Long userId, ProjectEventType type, String message);
}
