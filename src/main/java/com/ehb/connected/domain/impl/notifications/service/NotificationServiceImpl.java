package com.ehb.connected.domain.impl.notifications.service;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.entities.Notification;
import com.ehb.connected.domain.impl.notifications.mappers.NotificationMapper;
import com.ehb.connected.domain.impl.notifications.repositories.NotificationRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService{
    private NotificationMapper notificationMapper;
    private NotificationRepository notificationRepository;


    @Override
    public void createNotification(User recipient, String message, String destinationUrl) {
        Notification notification = new Notification();

        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setDestinationUrl(destinationUrl);

        notificationRepository.save(notification);
    }

    @Override
    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id);
        return notificationMapper.NotificationToDto(notification);
    }

    @Override
    public List<NotificationDto> getAllNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(notificationMapper::NotificationToDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDto updateNotification(Long id, NotificationDto notificationDto) {
        Notification notification = notificationRepository.findById(id);
        notification.setMessage(notificationDto.getMessage());
        notification.setRead(notificationDto.isRead());
        notificationRepository.save(notification);
        return notificationMapper.NotificationToDto(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
