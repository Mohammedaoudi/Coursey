package com.ensa.projet.notificationsservice.service.interfaces;

import com.ensa.projet.notificationsservice.model.NotificationResponse;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    List<NotificationResponse> getUserNotifications(Integer userId);
    List<NotificationResponse> getUnreadNotifications(Integer userId);
    Long getUnreadCount(Integer userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Integer userId);
    void processTestResultNotification(Map<String, Object> event);
    void processCertificateNotification(Map<String, Object> event);
    void deleteNotification(Long notificationId);

}