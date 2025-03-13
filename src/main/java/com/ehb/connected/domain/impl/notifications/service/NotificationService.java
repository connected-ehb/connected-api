package com.ehb.connected.domain.impl.notifications.service;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.users.entities.User;

import java.util.List;

public interface NotificationService {
    void createNotification(User recipient, String message, String destinationUrl);
    NotificationDto getNotificationById(Long id);
    List<NotificationDto> getAllNotificationsByUserId(Long userId);
    NotificationDto markNotificationAsRead(Long notificationId);
    void deleteNotification(Long id);
}
