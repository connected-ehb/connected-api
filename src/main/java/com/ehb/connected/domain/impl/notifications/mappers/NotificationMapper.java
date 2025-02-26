package com.ehb.connected.domain.impl.notifications.mappers;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.entities.Notification;
import com.ehb.connected.domain.impl.users.mappers.UserDetailsMapper;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {


    private final UserDetailsMapper userDetailsMapper;

    public NotificationMapper(UserDetailsMapper userDetailsMapper) {
        this.userDetailsMapper = userDetailsMapper;
    }

    public NotificationDto NotificationToDto(Notification notification){
        NotificationDto dto = new NotificationDto();
        dto.setNotificationId(notification.getId());
        userDetailsMapper.toUserDetailsDto(notification.getUser());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setDestinationUrl(notification.getDestinationUrl());
        dto.setTimestamp(notification.getTimestamp());
        return dto;
    }

    public Notification DtoToNotification(NotificationDto dto) {
        Notification notification = new Notification();
        notification.setId(dto.getNotificationId());
        notification.setUser(dto.getUser());
        notification.setMessage(dto.getMessage());
        notification.setIsRead(dto.getIsRead());
        notification.setDestinationUrl(dto.getDestinationUrl());
        notification.setTimestamp(dto.getTimestamp());
        return notification;
    }

}
