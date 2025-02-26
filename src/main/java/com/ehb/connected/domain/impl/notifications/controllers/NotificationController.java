package com.ehb.connected.domain.impl.notifications.controllers;

import com.ehb.connected.domain.impl.notifications.dto.NotificationDto;
import com.ehb.connected.domain.impl.notifications.service.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @PreAuthorize("hasAuthority('notification:read')")
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PreAuthorize("hasAuthority('notification:read')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getAllNotificationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getAllNotificationsByUserId(userId));
    }

    @PreAuthorize("hasAuthority('notification:read')")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDto> markNotificationAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markNotificationAsRead(notificationId));
    }

    @PreAuthorize("hasAuthority('notification:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
