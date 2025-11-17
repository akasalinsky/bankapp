package com.example.cash_service.service;

import com.example.cash_service.model.CashRequest;
import com.example.cash_service.model.Currency;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;



@Service
public class CashService {
    private final RestTemplate restTemplate;
    private final String gatewayUrl = "http://gateway";
    private final KeycloakAuthService authService;

    public CashService(RestTemplate restTemplate, KeycloakAuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    public void deposit(String login, CashRequest request, String jwtToken) {
        System.out.println("логин: " + login);
        System.out.println("валюта: " + request.getCurrency());
        System.out.println("тип операции: " + request.getOperationType());
        System.out.println("сумма: " + request.getAmount());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI depositUrl = UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                    .path("/api/accounts/{login}/deposit")
                    .queryParam("amount", request.getAmount())
                    .queryParam("currency", request.getCurrency())
                    .build(login);

            ResponseEntity<String> response = restTemplate.exchange(
                    depositUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Успешное пополнение счета");
                sendNotification(login, "DEPOSIT", "Счет пополнен на " + request.getAmount() + " " + request.getCurrency());
            } else {
                System.out.println("❌ Ошибка при пополнении: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("Accounts service returned error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.out.println("❌ Исключение в методе deposit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Deposit failed: " + e.getMessage(), e);
        }
    }

    public void withdraw(String login, CashRequest request, String jwtToken) {
        System.out.println("логин: " + login);
        System.out.println("валюта: " + request.getCurrency());
        System.out.println("тип операции: " + request.getOperationType());
        System.out.println("сумма: " + request.getAmount());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            URI depositUrl = UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                    .path("/api/accounts/{login}/withdraw")
                    .queryParam("amount", request.getAmount())
                    .queryParam("currency", request.getCurrency())
                    .build(login);

            ResponseEntity<String> response = restTemplate.exchange(
                    depositUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Успешное снятие со счета");
                sendNotification(login, "WITHDRAW", "Со счета снято " + request.getAmount() + " " + request.getCurrency());
            } else {
                System.out.println("❌ Ошибка при снятии: " + response.getStatusCode() + " - " + response.getBody());
                throw new RuntimeException("Accounts service returned error: " + response.getStatusCode() + " - " + response.getBody());
            }

        } catch (Exception e) {
            System.out.println("❌ Исключение в методе withdraw: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("withdraw failed: " + e.getMessage(), e);
        }
    }

    private void validateRequest(CashRequest request) {
        if (request.getUser() == null) {
            throw new IllegalArgumentException("Account ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.getCurrency() == null || !isValidCurrency(request.getCurrency())) {
            throw new IllegalArgumentException("Invalid currency: " + request.getCurrency());
        }
    }
    private boolean isValidCurrency(String currency) {
        return "RUB".equals(currency) || "USD".equals(currency) || "CNY".equals(currency);
    }

    private void sendNotification(String login, String type, String message) {
        try {
            URI notificationUrl = UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                    .path("/api/notifications")
                    .queryParam("login", login)
                    .queryParam("type", type)
                    .queryParam("message", message)
                    .build()
                    .toUri();

            restTemplate.postForObject(notificationUrl, null, Void.class);
            System.out.println("✅ Cash Service → Notification Service: " + message);

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем основную операцию
            System.out.println("❌ Cash Service → Notification Service failed: " + e.getMessage());
        }
    }


}
