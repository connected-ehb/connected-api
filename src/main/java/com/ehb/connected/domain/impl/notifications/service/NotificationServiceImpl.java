package com.ehb.connected.domain.impl.notifications.service;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.entities.Notification;
import com.ehb.connected.domain.impl.notifications.mappers.NotificationMapper;
import com.ehb.connected.domain.impl.notifications.repositories.NotificationRepository;
import com.ehb.connected.domain.impl.users.entities.User;
import com.ehb.connected.exceptions.EntityNotFoundException;
import com.ehb.connected.websockets.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService{
    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;
    private final WebSocketService webSocketService;

    @Autowired
    public NotificationServiceImpl(NotificationMapper notificationMapper, NotificationRepository notificationRepository, WebSocketService webSocketService) {
        this.notificationMapper = notificationMapper;
        this.notificationRepository = notificationRepository;
        this.webSocketService = webSocketService;
    }

    @Override
    public void createNotification(User recipient, String message, String destinationUrl) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setDestinationUrl(destinationUrl);
        notificationRepository.save(notification);
        //create notificationDto and send it to the recipient via the destinationUrl
        NotificationDto notificationDto = notificationMapper.NotificationToDto(notification);
        webSocketService.sendNotification("/user/"+ recipient.getId()+ "/notifications", notificationDto);
    }

    @Override
    public NotificationDto getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        return notificationMapper.NotificationToDto(notification);
    }

    @Override
    public List<NotificationDto> getAllNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(notificationMapper::NotificationToDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationDto markNotificationAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NotificationServiceImpl.class, notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return notificationMapper.NotificationToDto(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}
