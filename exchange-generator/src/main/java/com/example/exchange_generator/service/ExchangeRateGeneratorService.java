package com.example.exchange_generator.service;

import com.example.exchange_generator.model.CurrencyPair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ExchangeRateGeneratorService {
    private final Random random = new Random();

    // Базовые курсы (на начало периода)
    private double usdToRub = 95.0;
    private double cnyToRub = 13.0;
    private double rubToUsd = 0.0105;
    private double rubToCny = 0.0769;
    private double usdToCny = 0.1368;
    private double cnyToUsd = 7.29;

    public List<CurrencyPair> generateRates() {
        List<CurrencyPair> rates = new ArrayList<>();

        // Генерируем случайные колебания (±0.5%)
        double fluctuation = 0.005; // 0.5%

        // USD → RUB
        usdToRub = applyFluctuation(usdToRub, fluctuation);
        rates.add(new CurrencyPair("USD", "RUB", usdToRub));

        // CNY → RUB
        cnyToRub = applyFluctuation(cnyToRub, fluctuation);
        rates.add(new CurrencyPair("CNY", "RUB", cnyToRub));

        // RUB → USD
        rubToUsd = applyFluctuation(rubToUsd, fluctuation);
        rates.add(new CurrencyPair("RUB", "USD", rubToUsd));

        // RUB → CNY
        rubToCny = applyFluctuation(rubToCny, fluctuation);
        rates.add(new CurrencyPair("RUB", "CNY", rubToCny));

        // USD → CNY
        usdToCny = applyFluctuation(usdToCny, fluctuation);
        rates.add(new CurrencyPair("USD", "CNY", usdToCny));

        // CNY → USD
        cnyToUsd = applyFluctuation(cnyToUsd, fluctuation);
        rates.add(new CurrencyPair("CNY", "USD", cnyToUsd));

        return rates;
    }

    private double applyFluctuation(double baseRate, double fluctuation) {
        double change = baseRate * fluctuation * (random.nextDouble() * 2 - 1); // ±fluctuation%
        return Math.max(0.01, baseRate + change); // Минимум 0.01
    }

    public double getUsdToRub() { return usdToRub; }
    public double getCnyToRub() { return cnyToRub; }
    public double getRubToUsd() { return rubToUsd; }
    public double getRubToCny() { return rubToCny; }
    public double getUsdToCny() { return usdToCny; }
    public double getCnyToUsd() { return cnyToUsd; }
}
