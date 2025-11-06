package com.example.accounts_service.model;

public enum Currency {
    RUB("Рубль", "₽");
    //USD("Доллар","$"),
    //CNY("Юань","¥");

    private final String title;
    private final String symbol;

    Currency(String title, String symbol){
        this.title = title;
        this.symbol = symbol;
    }

    public String getTitle() {
        return title;
    }

    public String getSymbol() {
        return symbol;
    }
}
