package com.example.cash_service.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.example.cash_service.config.KeycloakConfig;

@Service
public class KeycloakTokenService {

    private final RestTemplate restTemplate;
    private final KeycloakConfig keycloakConfig;

    // Кэш для токена (опционально)
    private String cachedToken;
    private long tokenExpiryTime;

    public KeycloakTokenService(RestTemplate restTemplate, KeycloakConfig keycloakConfig) {
        this.restTemplate = restTemplate;
        this.keycloakConfig = keycloakConfig;
    }

    public String getClientCredentialsToken() {
        // Если токен в кэше и еще не истек, возвращаем его
        if (cachedToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return cachedToken;
        }

        try {
            // Подготавливаем заголовки
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Подготавливаем тело запроса
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "client_credentials");
            body.add("client_id", keycloakConfig.getClientId());
            body.add("client_secret", keycloakConfig.getClientSecret());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Отправляем запрос к Keycloak
            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                    keycloakConfig.getTokenUrl(),
                    HttpMethod.POST,
                    request,
                    TokenResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TokenResponse tokenResponse = response.getBody();
                cachedToken = tokenResponse.getAccessToken();

                // Устанавливаем время истечения токена (минус 10 секунд для запаса)
                tokenExpiryTime = System.currentTimeMillis() + (tokenResponse.getExpiresIn() - 10) * 1000;

                System.out.println("✅ Successfully obtained client credentials token");
                return cachedToken;
            } else {
                throw new RuntimeException("Failed to get token: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("❌ Error getting client credentials token: " + e.getMessage());
            throw new RuntimeException("Unable to obtain client credentials token", e);
        }
    }

    // DTO для ответа Keycloak
    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
        private int expiresIn;
        private String scope;
        private int refreshExpiresIn;

        // Геттеры и сеттеры
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public int getExpiresIn() { return expiresIn; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }

        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }

        public int getRefreshExpiresIn() { return refreshExpiresIn; }
        public void setRefreshExpiresIn(int refreshExpiresIn) { this.refreshExpiresIn = refreshExpiresIn; }
    }
}