package com.example.accounts_service.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationMessage {
    private String type;
    private String userId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
    private String message;
}