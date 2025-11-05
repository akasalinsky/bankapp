package com.example.cash_service.model;

import java.math.BigDecimal;



public class CashRequest {
    private String login;
    private BigDecimal amount;
    private String currency;
    private String operationType;

    public CashRequest() {
    }

    public CashRequest(String user, BigDecimal amount, String currency, String operationType) {
        this.login = user;
        this.amount = amount;
        this.currency = currency;
        this.operationType = operationType;
    }

    public String getUser() {
        return login;
    }

    public void setUser(String login) {
        this.login = login;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOperationType() { return operationType; }

    public void setOperationType(String operationType) { this.operationType = operationType; }
}
