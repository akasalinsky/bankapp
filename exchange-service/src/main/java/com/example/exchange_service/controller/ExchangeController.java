package com.example.exchange_service.controller;

import com.example.exchange_service.model.Currency;
import com.example.exchange_service.model.ExchangeRate;
import com.example.exchange_service.model.ExchangeRateDTO;
import com.example.exchange_service.service.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    @GetMapping("/rates")
    public ResponseEntity<List<ExchangeRate>> getCurrentRates() {
        List<ExchangeRate> rates = exchangeService.getAllCurrentRates();
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/rate/{from}/{to}")
    public ResponseEntity<ExchangeRate> getRate(
            @PathVariable String from,
            @PathVariable String to) {

        try {
            Currency fromCurrency = Currency.valueOf(from.toUpperCase());
            Currency toCurrency = Currency.valueOf(to.toUpperCase());

            Optional<ExchangeRate> rateOpt = exchangeService.getRate(fromCurrency, toCurrency);
            return rateOpt.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ExchangeRateDTO convertToDto(ExchangeRate rate) {
        ExchangeRateDTO dto = new ExchangeRateDTO();
        dto.setTitle(rate.getFromCurrency().getTitle());  // ← rate.title
        dto.setName(rate.getFromCurrency().name());       // ← rate.name
        dto.setValue(rate.getRate());                    // ← rate.value
        return dto;
    }

    @PostMapping("/rate")
    public ResponseEntity<?> updateRate(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal rate) {

        try {
            Currency fromCurrency = Currency.valueOf(from.toUpperCase());
            Currency toCurrency = Currency.valueOf(to.toUpperCase());

            exchangeService.updateRate(fromCurrency, toCurrency, rate);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/convert")
    public ResponseEntity<BigDecimal> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {

        try {
            Currency fromCurrency = Currency.valueOf(from.toUpperCase());
            Currency toCurrency = Currency.valueOf(to.toUpperCase());

            BigDecimal result = exchangeService.convert(fromCurrency, toCurrency, amount);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/rates")
    public ResponseEntity<?> receiveRate(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal rate) {
        System.out.println("Курсы:" + from + to + rate);

        try {
            Currency fromCurrency = Currency.valueOf(from.toUpperCase());
            Currency toCurrency = Currency.valueOf(to.toUpperCase());

            // Обновляем курс в БД
            exchangeService.updateRate(fromCurrency, toCurrency, rate);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }




}