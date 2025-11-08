package com.ehb.connected.domain.impl.projects.events.service;

import com.ehb.connected.domain.impl.projects.events.dto.ProjectEventDetailsDto;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEvent;
import com.ehb.connected.domain.impl.projects.events.entities.ProjectEventType;
import com.ehb.connected.domain.impl.projects.events.mappers.ProjectEventMapper;
import com.ehb.connected.domain.impl.projects.events.repositories.ProjectEventRepository;
import com.ehb.connected.domain.impl.projects.repositories.ProjectRepository;
import com.ehb.connected.domain.impl.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectEventServiceImpl implements ProjectEventService {

    private final ProjectEventRepository projectEventRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectEventMapper eventMapper;

    @Override
    public List<ProjectEventDetailsDto> getEventsForProject(Authentication authentication, Long projectId) {
        return projectEventRepository.findAllByProjectIdOrderByTimestampDesc(projectId)
                .stream()
                .map(eventMapper::toDetailsDto)
                .toList();
    }

    @Override
    public void logEvent(Long projectId, Long userId, ProjectEventType type, String message) {
        ProjectEvent event = new ProjectEvent();
        event.setProject(projectRepository.getReferenceById(projectId));
        event.setActor(userId != null ? userRepository.getReferenceById(userId) : null);
        event.setType(type);
        event.setMessage(message);
        projectEventRepository.save(event);
    }
}
