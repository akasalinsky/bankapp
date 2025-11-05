package com.example.notification_service.model;

public enum NotificationType {
    DEPOSIT("Пополнение счета"),
    WITHDRAW("Снятие со счета"),
    TRANSFER("Перевод средств");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}