package com.ehb.connected.domain.impl.notifications.dto;

import com.ehb.connected.domain.impl.users.entities.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDto {
    private Long notificationId;
    private User user;
    private String message;
    private Boolean isRead;
    private LocalDateTime timestamp;
    private String destinationUrl;
}
