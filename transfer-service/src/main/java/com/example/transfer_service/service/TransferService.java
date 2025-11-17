package com.example.transfer_service.service;

import com.example.transfer_service.model.TransferRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TransferService {

    private final RestTemplate restTemplate;
    private final String gatewayUrl = "http://gateway";


    public TransferService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void internalTransfer(TransferRequest request) {
        validateRequest(request);

        try {
            String withdrawUrl = gatewayUrl + "/api/accounts/" +
                    request.getFromAccountId() + "/withdraw?amount=" +
                    request.getAmount() + "&currency=" + request.getCurrency();

            restTemplate.postForObject(withdrawUrl, null, Void.class);

            // Пополнение второго счета
            String depositUrl = gatewayUrl + "/api/accounts/" +
                    request.getToAccountId() + "/deposit?amount=" +
                    request.getAmount() + "&currency=" + request.getCurrency();

            restTemplate.postForObject(depositUrl, null, Void.class);

        } catch (Exception e) {
            // Откат операции (в реальном приложении)
            rollbackTransfer(request);
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }
    }

    public void externalTransfer(TransferRequest request) {
        internalTransfer(request);
    }

    private void validateRequest(TransferRequest request) {
        if (request.getFromAccountId() == null || request.getToAccountId() == null) {
            throw new IllegalArgumentException("Account IDs are required");
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

    private void rollbackTransfer(TransferRequest request) {
        try {
            String rollbackDepositUrl = gatewayUrl + "/api/accounts/" +
                    request.getToAccountId() + "/withdraw?amount=" +
                    request.getAmount() + "&currency=" + request.getCurrency();

            restTemplate.postForObject(rollbackDepositUrl, null, Void.class);

        } catch (Exception rollbackException) {
            System.err.println("Rollback failed: " + rollbackException.getMessage());
        }
    }
}