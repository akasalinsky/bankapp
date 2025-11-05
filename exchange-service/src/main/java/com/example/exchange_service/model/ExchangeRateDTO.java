package com.example.exchange_service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO для JavaScript (с нужными полями!)
public class ExchangeRateDTO {
    private String title;    // ← rate.title (например: "Доллар")
    private String name;     // ← rate.name (например: "USD")
    private BigDecimal value; // ← rate.value (например: 95.50)

    // Constructors
    public ExchangeRateDTO() {}

    public ExchangeRateDTO(String title, String name, BigDecimal value) {
        this.title = title;
        this.name = name;
        this.value = value;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}