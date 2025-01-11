package com.ensa.projet.notificationsservice.kafka;

import com.ensa.projet.notificationsservice.service.interfaces.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationKafkaListeners {

    private final NotificationService notificationService;

    public NotificationKafkaListeners(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
            topics = "test-results",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory" // Add this
    )
    public void handleTestResult(@Payload Map<String, Object> message) { // Changed from String to Map
        try {
            notificationService.processTestResultNotification(message);
        } catch (Exception e) {
            // Handle error
        }
    }

    @KafkaListener(
            topics = "certificates",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory" // Add this
    )
    public void handleCertificate(@Payload Map<String, Object> message) { // Changed from String to Map
        try {
            notificationService.processCertificateNotification(message);
        } catch (Exception e) {
            // Handle error
        }
    }
}