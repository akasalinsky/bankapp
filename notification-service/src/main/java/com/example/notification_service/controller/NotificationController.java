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
    public ResponseEntity<?> sendNotificationWithKeycloak(
            @RequestParam String keycloakId,
            @RequestParam String userLogin,
            @RequestParam NotificationType type,
            @RequestParam String message) {

        try {
            notificationService.sendNotification(keycloakId, userLogin, type, message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/keycloak/{keycloakId}")
    public ResponseEntity<List<Notification>> getNotificationsByKeycloakId(
            @PathVariable String keycloakId) {

        try {
            List<Notification> notifications = notificationService.getNotificationsByKeycloakId(keycloakId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}