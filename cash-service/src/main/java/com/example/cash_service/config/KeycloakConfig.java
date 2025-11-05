package com.example.cash_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    // ИСПРАВЛЕНО: Правильный URL Keycloak (порт 8085!)
    @Value("${keycloak.url:http://localhost:8085}")
    private String keycloakUrl;

    // ИСПРАВЛЕНО: Правильный Realm
    @Value("${keycloak.realm:bank-realm}")
    private String realm;

    // ИСПРАВЛЕНО: Правильный Client ID
    @Value("${keycloak.client.id:bank-client}")
    private String clientId;

    // ИСПРАВЛЕНО: Правильный Client Secret
    @Value("${keycloak.client.secret:secret}")
    private String clientSecret;

    // Геттеры
    public String getKeycloakUrl() { return keycloakUrl; }
    public String getRealm() { return realm; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }

    // URL для получения токенов
    public String getTokenUrl() {
        return keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }
}