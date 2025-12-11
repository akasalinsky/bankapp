package com.example.notification_service.kafka;

import com.example.notification_service.kafka.dto.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationConsumer {

    @KafkaListener(
        topics = "${kafka.topic.notifications}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotification(NotificationMessage message) {
        log.info("Received notification: Type={}, User={}, Message={}", 
            message.getType(), message.getUserId(), message.getMessage());
        
        // TODO: Отправить email, SMS, или сохранить в БД
        processNotification(message);
    }

    private void processNotification(NotificationMessage message) {
        // Implement notification logic here
        log.info("Processing notification for user: {}", message.getUserId());
        // Example: emailService.send(message.getUserId(), message.getMessage());
    }
}
