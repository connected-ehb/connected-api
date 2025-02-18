package com.ehb.connected.domain.impl.notifications.service;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;

import java.util.List;

public interface NotificationService {
    NotificationDto createNotification(NotificationDto notificationDto);
    NotificationDto getNotificationById(Long id);
    List<NotificationDto> getAllNotificationsByUserId(Long userId);
    NotificationDto updateNotification(Long id, NotificationDto notificationDto);
    void deleteNotification(Long id);
}
