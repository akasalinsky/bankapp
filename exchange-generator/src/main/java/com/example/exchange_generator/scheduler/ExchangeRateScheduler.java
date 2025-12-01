package com.example.exchange_generator.scheduler;

import com.example.exchange_generator.model.CurrencyPair;
import com.example.exchange_generator.service.ExchangeRateGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ExchangeRateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateScheduler.class);

    private final ExchangeRateGeneratorService rateGeneratorService;
    private final RestTemplate restTemplate;
    private final String exchangeServiceUrl = "http://exchange-service";

    public ExchangeRateScheduler(
            ExchangeRateGeneratorService rateGeneratorService,
            RestTemplate restTemplate) {
        this.rateGeneratorService = rateGeneratorService;
        this.restTemplate = restTemplate;
    }

    // Генерируем и отправляем курсы каждую секунду
    @Scheduled(fixedRate = 10000)  // 1000 мс = 1 секунда
    public void generateAndSendRates() {
        try {
            // Генерируем новые курсы
            List<CurrencyPair> rates = rateGeneratorService.generateRates();

            // Отправляем каждый курс в Exchange Service
            for (CurrencyPair pair : rates) {
                sendRate(pair);
            }

            logger.info("Generated and sent {} exchange rates", rates.size());

        } catch (Exception e) {
            logger.error("Failed to generate/send exchange rates", e);
        }
    }

    private void sendRate(CurrencyPair pair) {
        try {
            String url = exchangeServiceUrl + "/api/exchange/rates" +
                    "?from=" + pair.getFromCurrency() +
                    "&to=" + pair.getToCurrency() +
                    "&rate=" + pair.getRate();

            restTemplate.postForObject(url, null, Void.class);
            logger.debug("Sent rate: {} -> {} = {}",
                    pair.getFromCurrency(),
                    pair.getToCurrency(),
                    pair.getRate());

        } catch (Exception e) {
            logger.error("Failed to send rate {} -> {}: {}",
                    pair.getFromCurrency(),
                    pair.getToCurrency(),
                    e.getMessage());
        }
    }
}