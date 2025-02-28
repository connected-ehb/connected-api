package com.ehb.connected.domain.impl.announcements.service;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementCreateDto;
import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import com.ehb.connected.domain.impl.announcements.entities.Announcement;
import com.ehb.connected.domain.impl.announcements.mappers.AnnouncementMapper;
import com.ehb.connected.domain.impl.announcements.repositories.AnnouncementRepository;
import com.ehb.connected.domain.impl.assignments.entities.Assignment;
import com.ehb.connected.domain.impl.assignments.repositories.AssignmentRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.domain.impl.users.services.UserService;
import com.ehb.connected.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;
    private final UserService userService;
    private final AssignmentRepository assignmentRepository;
    @Override
    public AnnouncementDetailsDto createAnnouncementByAssignment(Principal principal, Long assignmentId, AnnouncementCreateDto announcement) {
        User user = userService.getUserByPrincipal(principal);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(Announcement.class, assignmentId));
        Announcement announcementEntity = new Announcement();

        announcementEntity.setTitle(announcement.getTitle());
        announcementEntity.setMessage(announcement.getMessage());
        announcementEntity.setCreatedBy(user);
        announcementEntity.setAssignment(assignment);
        announcementRepository.save(announcementEntity);

        return announcementMapper.toDto(announcementEntity);
    }

    @Override
    public List<AnnouncementDetailsDto> getAnnouncementsByAssignment(Principal principal, Long assignmentId) {
        return announcementMapper.toDtoList(announcementRepository.findAllByAssignmentIdOrderByCreatedAtDesc(assignmentId));
    }
}
