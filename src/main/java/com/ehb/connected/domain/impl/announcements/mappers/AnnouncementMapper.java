package com.ehb.connected.domain.impl.announcements.mappers;

import com.ehb.connected.domain.impl.announcements.dto.AnnouncementDetailsDto;
import com.ehb.connected.domain.impl.announcements.entities.Announcement;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnnouncementMapper {
    private final UserDetailsMapper userDetailsMapper;

    public AnnouncementDetailsDto toDto(Announcement announcement) {
        AnnouncementDetailsDto announcementDetailsDto = new AnnouncementDetailsDto(
                announcement.getId(),
                announcement.getAssignment().getId(),
                announcement.getTitle(),
                announcement.getMessage(),
                announcement.getCreatedAt(),
                announcement.getCreatedBy() != null ? userDetailsMapper.toUserDetailsDto(announcement.getCreatedBy()) : null
        );
        return announcementDetailsDto;
    }

    public List<AnnouncementDetailsDto> toDtoList(List<Announcement> announcements) {
        return announcements.stream().map(this::toDto).toList();
    }
}
