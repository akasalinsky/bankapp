package com.example.cash_service.service;
import com.example.cash_service.model.CashRequest;
import com.example.cash_service.model.Currency; // Импортируем enum, если он используется в других частях приложения
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KeycloakAuthService authService; // Предполагается, что он есть

    private CashService cashService;

    private final String gatewayUrl = "http://gateway";
    private final String testToken = "test-jwt-token";
    private final String testLogin = "testUser";

    @BeforeEach
    void setUp() {
        cashService = new CashService(restTemplate, authService);
    }

    @Test
    void deposit_Successful() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("100.00"));
        // ИСПРАВЛЕНИЕ: передаем строку, а не enum
        request.setCurrency("RUB");
        request.setOperationType("DEPOSIT");

        // Мокаем успешный ответ от accounts-service
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Deposit successful", HttpStatus.OK);
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Выполняем тестируемый метод
        assertDoesNotThrow(() -> cashService.deposit(testLogin, request, testToken));

        // Проверяем вызов restTemplate.exchange
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate).exchange(
                uriCaptor.capture(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        // Проверяем URL
        URI capturedUri = uriCaptor.getValue();
        // ИСПРАВЛЕНИЕ: в URL будет "RUB", а не Currency.RUB.toString()
        assertEquals(gatewayUrl + "/api/accounts/" + testLogin + "/deposit?amount=100.00&currency=RUB", capturedUri.toString());

        // Проверяем заголовки
        HttpEntity capturedEntity = entityCaptor.getValue();
        HttpHeaders capturedHeaders = capturedEntity.getHeaders();
        assertEquals("Bearer " + testToken, capturedHeaders.getFirst("Authorization"));

        // Проверяем вызов sendNotification через restTemplate.postForObject
        // ИСПРАВЛЕНИЕ: в сообщении будет "RUB", а не Currency.RUB.toString()
        verify(restTemplate).postForObject(
                eq(UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                        .path("/api/notifications")
                        .queryParam("login", testLogin)
                        .queryParam("type", "DEPOSIT")
                        .queryParam("message", "Счет пополнен на " + request.getAmount() + " " + request.getCurrency())
                        .build().toUri()),
                eq(null),
                eq(Void.class)
        );
    }

    @Test
    void deposit_AccountsServiceReturnsError_ThrowsException() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("RUB"); // ИСПРАВЛЕНИЕ
        request.setOperationType("DEPOSIT");

        // Мокаем ошибочный ответ от accounts-service
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Проверяем, что метод выбрасывает RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                cashService.deposit(testLogin, request, testToken)
        );

        assertTrue(thrown.getMessage().contains("Accounts service returned error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

        // Проверяем, что sendNotification НЕ был вызван в случае ошибки от accounts-service
        verify(restTemplate, never()).postForObject(
                any(URI.class),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void deposit_RestTemplateThrowsException_ThrowsException() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("RUB"); // ИСПРАВЛЕНИЕ
        request.setOperationType("DEPOSIT");

        // Мокаем выброс исключения из restTemplate
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Network error"));

        // Проверяем, что метод выбрасывает RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                cashService.deposit(testLogin, request, testToken)
        );

        assertTrue(thrown.getMessage().contains("Deposit failed"));
        assertTrue(thrown.getCause() instanceof RuntimeException);
        assertEquals("Network error", thrown.getCause().getMessage());

        // Проверяем, что sendNotification НЕ был вызван в случае исключения
        verify(restTemplate, never()).postForObject(
                any(URI.class),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void withdraw_Successful() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("50.00"));
        // ИСПРАВЛЕНИЕ: передаем строку, а не enum
        request.setCurrency("USD");
        request.setOperationType("WITHDRAW");

        // Мокаем успешный ответ от accounts-service
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Withdraw successful", HttpStatus.OK);
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Выполняем тестируемый метод
        assertDoesNotThrow(() -> cashService.withdraw(testLogin, request, testToken));

        // Проверяем вызов restTemplate.exchange
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);

        verify(restTemplate).exchange(
                uriCaptor.capture(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class)
        );

        // Проверяем URL
        URI capturedUri = uriCaptor.getValue();
        // ИСПРАВЛЕНИЕ: в URL будет "USD", а не Currency.USD.toString()
        assertEquals(gatewayUrl + "/api/accounts/" + testLogin + "/withdraw?amount=50.00&currency=USD", capturedUri.toString());

        // Проверяем заголовки
        HttpEntity capturedEntity = entityCaptor.getValue();
        HttpHeaders capturedHeaders = capturedEntity.getHeaders();
        assertEquals("Bearer " + testToken, capturedHeaders.getFirst("Authorization"));

        // Проверяем вызов sendNotification через restTemplate.postForObject
        // ИСПРАВЛЕНИЕ: в сообщении будет "USD", а не Currency.USD.toString()
        verify(restTemplate).postForObject(
                eq(UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                        .path("/api/notifications")
                        .queryParam("login", testLogin)
                        .queryParam("type", "WITHDRAW")
                        .queryParam("message", "Со счета снято " + request.getAmount() + " " + request.getCurrency())
                        .build().toUri()),
                eq(null),
                eq(Void.class)
        );
    }

    @Test
    void withdraw_AccountsServiceReturnsError_ThrowsException() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("USD"); // ИСПРАВЛЕНИЕ
        request.setOperationType("WITHDRAW");

        // Мокаем ошибочный ответ от accounts-service
        ResponseEntity<String> responseEntity = new ResponseEntity<>("Error", HttpStatus.FORBIDDEN);
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Проверяем, что метод выбрасывает RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                cashService.withdraw(testLogin, request, testToken)
        );

        assertTrue(thrown.getMessage().contains("Accounts service returned error"));
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());

        // Проверяем, что sendNotification НЕ был вызван в случае ошибки от accounts-service
        verify(restTemplate, never()).postForObject(
                any(URI.class),
                any(),
                eq(Void.class)
        );
    }

    @Test
    void withdraw_RestTemplateThrowsException_ThrowsException() {
        CashRequest request = new CashRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setCurrency("USD"); // ИСПРАВЛЕНИЕ
        request.setOperationType("WITHDRAW");

        // Мокаем выброс исключения из restTemplate
        when(restTemplate.exchange(
                any(URI.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RuntimeException("Network error"));

        // Проверяем, что метод выбрасывает RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () ->
                cashService.withdraw(testLogin, request, testToken)
        );

        assertTrue(thrown.getMessage().contains("withdraw failed"));
        assertTrue(thrown.getCause() instanceof RuntimeException);
        assertEquals("Network error", thrown.getCause().getMessage());

        // Проверяем, что sendNotification НЕ был вызван в случае исключения
        verify(restTemplate, never()).postForObject(
                any(URI.class),
                any(),
                eq(Void.class)
        );
    }
}