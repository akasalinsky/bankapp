package com.example.exchange_service.service;

import com.example.exchange_service.model.Currency;
import com.example.exchange_service.model.ExchangeRate;
import com.example.exchange_service.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ExchangeService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    // ✅ Обновить курс (удалить старый, сохранить новый):
    public void updateRate(Currency from, Currency to, BigDecimal rate) {
        System.out.println("апдейт");
        // Удаляем старый курс (если есть)
        Optional<ExchangeRate> existingRateOpt = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, to);

        if (existingRateOpt.isPresent()) {
            // Обновляем существующий курс
            ExchangeRate existingRate = existingRateOpt.get();
            existingRate.setRate(rate);
            exchangeRateRepository.save(existingRate);
            System.out.println("Курс обновлен");
        } else {
            // Создаем новый курс
            ExchangeRate newRate = new ExchangeRate(from, to, rate);
            exchangeRateRepository.save(newRate);
            System.out.println("Новый курс создан");
        }
    }

    // ✅ Получить курс:
    public Optional<ExchangeRate> getRate(Currency from, Currency to) {
        return exchangeRateRepository.findByFromCurrencyAndToCurrency(from, to);
    }

    // ✅ Получить все текущие курсы:
    public List<ExchangeRate> getAllCurrentRates() {
        return exchangeRateRepository.findAll();
    }

    // ✅ Конвертация валюты:
    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        if (from == to) return amount;

        Optional<ExchangeRate> rateOpt = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, to);

        if (rateOpt.isPresent()) {
            return amount.multiply(rateOpt.get().getRate())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Если прямого курса нет - через RUB как промежуточную валюту
        return convertViaRub(from, to, amount);
    }

    private BigDecimal convertViaRub(Currency from, Currency to, BigDecimal amount) {
        // Конвертируем из from в RUB
        Optional<ExchangeRate> rateToRubOpt = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(from, Currency.RUB);

        if (rateToRubOpt.isEmpty()) {
            throw new RuntimeException("No exchange rate found for " + from + " to RUB");
        }

        BigDecimal rubAmount = amount.multiply(rateToRubOpt.get().getRate());

        // Конвертируем из RUB в to
        Optional<ExchangeRate> rateFromRubOpt = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(Currency.RUB, to);

        if (rateFromRubOpt.isEmpty()) {
            throw new RuntimeException("No exchange rate found for RUB to " + to);
        }

        return rubAmount.multiply(rateFromRubOpt.get().getRate())
                .setScale(2, RoundingMode.HALF_UP);
    }

}