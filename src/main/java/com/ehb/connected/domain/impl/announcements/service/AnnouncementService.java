package com.ehb.connected.domain.impl.announcements.service;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementCreateDto;
import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;

import java.security.Principal;
import java.util.List;

public interface AnnouncementService {
    AnnouncementDetailsDto createAnnouncementByAssignment(Principal principal, Long assignmentId, AnnouncementCreateDto announcement);

    List<AnnouncementDetailsDto> getAnnouncementsByAssignment(Principal principal, Long assignmentId);
}
