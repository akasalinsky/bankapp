package com.example.front_ui.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Модель для получения данных от Exchange Service
public class ExchangeRate {

    private String title;    // Название валюты ("Доллар", "Рубль")
    private String name;     // Код валюты ("USD", "RUB")
    private BigDecimal value; // Значение курса (95.50, 1.00)
    private String fromCurrency; // Исходная валюта
    private String toCurrency;   // Целевая валюта
    private LocalDateTime timestamp; // Время обновления курса

    // Constructors
    public ExchangeRate() {}

    /*public ExchangeRate(String title, String name, BigDecimal value) {
        this.title = title;
        this.name = name;
        this.value = value;
    }
*/
    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal value) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.value = value;
        this.title = fromCurrency;
        this.name = toCurrency;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}