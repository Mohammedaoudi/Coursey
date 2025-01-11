package com.ensa.projet.participantservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTestResultNotification(Integer participantId, Integer trainingId, Float score) {
        Map<String, Object> event = new HashMap<>();
        event.put("participantId", participantId);
        event.put("trainingId", trainingId);
        event.put("score", score);
        event.put("title", "Test Results Ready");
        event.put("message", "Your test results are now available");

        log.info("Sending test result notification for participant: {}", participantId);
        kafkaTemplate.send("test-results", event);
    }

    public void sendCertificationNotification(Integer participantId, Integer trainingId, String certificateNumber) {
        Map<String, Object> event = new HashMap<>();
        event.put("participantId", participantId);
        event.put("trainingId", trainingId);
        event.put("certificateNumber", certificateNumber);
        event.put("title", "Certificate Ready");
        event.put("message", "Congratulations! Your First Aid certificate is ready for download");

        log.info("Sending certification notification for participant: {}", participantId);
        kafkaTemplate.send("certificates", event);
    }
}