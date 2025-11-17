package com.example.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.notification_service.model.NotificationType;

@Service
public class NotificationServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceClient.class);

    @Autowired
    private RestTemplate restTemplate;

    public void sendNotification(String keycloakId, String userLogin, NotificationType type, String message) {
        try {
            String url = "http://notification-service/api/notifications/keycloak?" +
                    "keycloakId={keycloakId}&userLogin={userLogin}&type={type}&message={message}";

            restTemplate.postForObject(url, null, String.class,
                    keycloakId, userLogin, type, message);

        } catch (Exception e) {
            logger.warn("Failed to send notification: {}", e.getMessage());
        }
    }
}