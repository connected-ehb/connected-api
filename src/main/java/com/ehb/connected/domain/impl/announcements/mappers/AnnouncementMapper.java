package com.ehb.connected.domain.impl.announcements.mappers;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import com.ehb.connected.domain.impl.announcements.entities.Announcement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnnouncementMapper {

    public AnnouncementDetailsDto toDto(Announcement announcement) {
        return new AnnouncementDetailsDto(
                announcement.getId(),
                announcement.getAssignment().getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getCreatedAt(),
                announcement.getCreatedBy()
        );
    }

    public List<AnnouncementDetailsDto> toDtoList(List<Announcement> announcements) {
        return announcements.stream().map(this::toDto).toList();
    }
}
