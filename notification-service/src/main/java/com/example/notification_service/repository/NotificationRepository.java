package com.example.notification_service.repository;

import com.example.notification_service.model.Notification;
import com.example.notification_service.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByKeycloakIdOrderByTimestampDesc(String keycloakId);
    List<Notification> findByUserLoginOrderByTimestampDesc(String userLogin);
}