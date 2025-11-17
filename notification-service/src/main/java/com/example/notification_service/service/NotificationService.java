package com.example.notification_service.service;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationType;
import com.example.notification_service.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void sendNotification(String keycloakId, String userLogin, NotificationType type, String message) {
        try {
            Notification notification = new Notification(keycloakId, userLogin, type, message);
            notification.setSent(true); // Отмечаем как "отправленное" (хотя это заглушка)

            notificationRepository.save(notification);
            logger.info("🔔 NOTIFICATION for {}: {} - {}", keycloakId, type.getDescription(), message);

        } catch (Exception e) {
            logger.error("Failed to save notification for {}: {}", keycloakId, e.getMessage());
        }
    }

    public List<Notification> getNotificationsByKeycloakId(String keycloakId) {
        try {
            return notificationRepository.findByKeycloakIdOrderByTimestampDesc(keycloakId);
        } catch (Exception e) {
            logger.error("Failed to get notifications for keycloakId {}: {}", keycloakId, e.getMessage());
            return List.of();
        }
    }
}