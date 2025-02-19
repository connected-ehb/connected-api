package com.ehb.connected.domain.impl.notifications.controllers;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationServiceImpl notificationService;

    public NotificationController(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getAllNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
    }

    //nodig voor Read boolean aan te passen
    //redirecten en read op true zetten
    @PutMapping("/{id}")
    public ResponseEntity<NotificationDto> updateNotification(@PathVariable Long id, @RequestBody NotificationDto notificationDto) {
        return ResponseEntity.ok(notificationService.updateNotification(id, notificationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
