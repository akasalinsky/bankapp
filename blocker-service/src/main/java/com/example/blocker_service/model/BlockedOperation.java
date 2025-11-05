package com.example.blocker_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BlockedOperation {
    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String operationType; // DEPOSIT, WITHDRAW, TRANSFER
    private String reason; // Причина блокировки
    private LocalDateTime timestamp;
    private boolean isBlocked;

    // Constructors
    public BlockedOperation() {}

    public BlockedOperation(Long accountId, BigDecimal amount, String currency,
                            String operationType, String reason) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.operationType = operationType;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
        this.isBlocked = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }
}