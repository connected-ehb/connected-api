package com.ehb.connected.domain.impl.notifications.dto;

import com.ehb.connected.domain.impl.users.entities.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class NotificationDto {
    private Long notificationId;
    private User user;
    private String message;
    private boolean read;
    private LocalDateTime timestamp;

}
