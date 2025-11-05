package com.example.front_ui.model;

import java.math.BigDecimal;

public class CashRequestDTO {
    private String login;
    private BigDecimal amount;
    private String currency;
    private String operationType;

    // Конструкторы
    public CashRequestDTO() {}

    public CashRequestDTO(String login, BigDecimal amount, String currency, String operationType) {
        this.login = login;
        this.amount = amount;
        this.currency = currency;
        this.operationType = operationType;
    }

    // Геттеры и сеттеры
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
}