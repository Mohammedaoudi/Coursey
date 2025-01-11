package com.ensa.projet.notificationsservice.service.implementation;

import com.ensa.projet.notificationsservice.model.Notification;
import com.ensa.projet.notificationsservice.model.NotificationResponse;
import com.ensa.projet.notificationsservice.model.NotificationType;
import com.ensa.projet.notificationsservice.repository.NotificationRepository;
import com.ensa.projet.notificationsservice.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Integer userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndReadedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndReadedFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setReaded(true);
                    notificationRepository.save(notification);
                });
    }
    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    @Override
    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    public void processTestResultNotification(Map<String, Object> event) {
        Integer userId = (Integer) event.get("participantId");
        Float score = ((Number) event.get("score")).floatValue();

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Test Results Ready");
        notification.setMessage(String.format("Your test results are ready. Score: %.1f%%", score));
        notification.setType(NotificationType.TEST_RESULT);
        notification.setReaded(false);

        notificationRepository.save(notification);
    }

    @Override
    public void processCertificateNotification(Map<String, Object> event) {
        Integer userId = (Integer) event.get("participantId");
        String certificateNumber = (String) event.get("certificateNumber");

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle("Certificate Ready");
        notification.setMessage("Congratulations! Your First Aid certificate is ready for download");
        notification.setType(NotificationType.CERTIFICATE_READY);
        notification.setReaded(false);

        notificationRepository.save(notification);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
        response.setRead(notification.isReaded());

        return response;
    }


    private String calculateTimeAgo(LocalDateTime dateTime) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());

        if (duration.toMinutes() < 60) {
            return duration.toMinutes() + "m ago";
        } else if (duration.toHours() < 24) {
            return duration.toHours() + "h ago";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd"));
        }
    }
}