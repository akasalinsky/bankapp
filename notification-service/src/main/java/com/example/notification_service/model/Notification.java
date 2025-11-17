package com.example.notification_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "message", length = 1000)
    private String message;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_sent", nullable = false)
    private boolean isSent = false;

    @Column(name = "user_login")
    private String userLogin;

    // Constructors
    public Notification() {}

    public Notification(String login, String userLogin, NotificationType type, String message) {
        this.keycloakId = keycloakId;
        this.type = type;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return keycloakId; }  // Заменили getAccountId на getLogin
    public void setLogin(String login) { this.keycloakId = login; }  // Заменили setAccountId на setLogin

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isSent() { return isSent; }
    public void setSent(boolean sent) { isSent = sent; }
}