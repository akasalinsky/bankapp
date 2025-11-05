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

    public void sendNotification(String login, NotificationType type, String message) {
        try {
            // Просто сохраняем уведомление в БД и логируем
            Notification notification = new Notification(login, type, message);
            notification.setSent(true); // Отмечаем как "отправленное" (хотя это заглушка)

            notificationRepository.save(notification);

            // Логируем в консоль (это и есть наша "отправка" по заданию)
            logger.info("🔔 NOTIFICATION for {}: {} - {}", login, type.getDescription(), message);

        } catch (Exception e) {
            logger.error("Failed to save notification for {}: {}", login, e.getMessage());
        }
    }

    public List<Notification> getNotificationsByLogin(String login) {
        try {
            return notificationRepository.findByLoginOrderByTimestampDesc(login);
        } catch (Exception e) {
            logger.error("Failed to get notifications for {}: {}", login, e.getMessage());
            return List.of();
        }
    }

    // Убираем все методы для email/sms - не нужны
}