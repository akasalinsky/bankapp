package com.example.accounts_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Feign-клиент для взаимодействия с сервисом курсов валют (exchange-service).
 * Определяет метод для конвертации валют.
 */
@FeignClient(name = "exchange-service") // Имя сервиса в Eureka/Service Registry
public interface ExchangeServiceClient {

    /**
     * Конвертирует заданную сумму из одной валюты в другую,
     * вызывая эндпоинт exchange-service.
     *
     * @param from Валюта источника (например, "USD").
     * @param to Валюта назначения (например, "EUR").
     * @param amount Сумма в исходной валюте.
     * @return Сконвертированная сумма в валюте назначения.
     */
    @GetMapping("/api/exchange/convert")
    BigDecimal convert(
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("amount") BigDecimal amount);
}
