package com.example.front_ui.model;

import java.math.BigDecimal;

public class AccountViewDTO {
    private String currency;
    private BigDecimal balance;
    private boolean exists;

    public AccountViewDTO(String currency, BigDecimal balance) {
        this.currency = currency;
        this.balance = balance;
        this.exists = balance != null && balance.compareTo(BigDecimal.ZERO) >= 0;
    }

    // Геттеры
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public boolean isExists() { return exists; }

    // Для совместимости с шаблоном
    public String getValue() {
        return balance != null ? balance.toString() : "0";
    }

    public Currency getCurrencyEnum() {
        return Currency.valueOf(currency);
    }
}