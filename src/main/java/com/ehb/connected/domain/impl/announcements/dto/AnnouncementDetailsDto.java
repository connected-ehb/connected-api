package com.ehb.connected.domain.impl.announcements.dto;

import com.ehb.connected.domain.impl.users.dto.UserDetailsDto;
import com.ehb.connected.domain.impl.users.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AnnouncementDetailsDto {

    private Long id;
    private Long assignmentId;
    private String title;
    private String message;
    private UserDetailsDto createdBy;
    private LocalDateTime createdAt;

    public AnnouncementDetailsDto(Long id, Long assignmentId, String title, String message, LocalDateTime createdAt, User createdBy) {
    }
}
