package com.ehb.connected.domain.impl.notifications.mappers;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {


    public NotificationDto NotificationToDto(Notification notification){
        NotificationDto dto = new NotificationDto();
        dto.setNotificationId(notification.getId());
        dto.setUser(notification.getUser());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setTimestamp(notification.getTimestamp());
        return dto;
    }

    public Notification DtoToNotification(NotificationDto dto) {
        Notification notification = new Notification();
        notification.setId(dto.getNotificationId());
        notification.setUser(dto.getUser());
        notification.setMessage(dto.getMessage());
        notification.setRead(dto.isRead());
        notification.setTimestamp(dto.getTimestamp());
        return notification;
    }

}
