package com.ensa.projet.notificationsservice.controller;


import com.ensa.projet.notificationsservice.model.NotificationResponse;
import com.ensa.projet.notificationsservice.service.interfaces.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable Integer userId,
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
        List<NotificationResponse> notifications = unreadOnly ?
                notificationService.getUnreadNotifications(userId) :
                notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable Integer userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}