package com.example.accounts_service.kafka;
import com.example.accounts_service.kafka.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.notifications}")

    private String notificationTopic;

    public void sendNotification(NotificationMessage message) {
        log.info("Sending notification to Kafka: {}", message);
        kafkaTemplate.send(notificationTopic, message.getUserId(), message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Notification sent successfully: {}", message.getType());
                    } else {
                        log.error("Failed to send notification", ex);
                    }
                });
    }
}