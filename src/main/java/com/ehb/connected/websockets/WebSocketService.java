package com.ehb.connected.websockets;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class WebSocketService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebSocketService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }


    //send a notification to a destination
    //future ad a String destination param to specify the destination
    public void sendNotification(String destination, NotificationDto notificationDto) {
        try {
            simpMessagingTemplate.convertAndSend(destination, notificationDto);
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error sending WebSocket notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
