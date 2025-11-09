package com.ehb.connected.domain.impl.announcements.service;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementCreateDto;
import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AnnouncementService {
    AnnouncementDetailsDto createAnnouncementByAssignment(Authentication authentication, Long assignmentId, AnnouncementCreateDto announcement);

    List<AnnouncementDetailsDto> getAnnouncementsByAssignment(Authentication authentication, Long assignmentId);
}
