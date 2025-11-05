package com.example.notification_service.controller;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationType;
import com.example.notification_service.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<?> sendNotification(
            @RequestParam String login,  // Заменили accountId на login
            @RequestParam NotificationType type,
            @RequestParam String message) {

        try {
            notificationService.sendNotification(login, type, message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<List<Notification>> getNotificationsByLogin(
            @PathVariable String login) {  // Заменили accountId на login

        try {
            List<Notification> notifications = notificationService.getNotificationsByLogin(login);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Убираем email/sms методы - они не нужны по минимальным требованиям
}