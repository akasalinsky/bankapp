package com.example.accounts_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.url:http://keycloak:8080}")
    private String keycloakUrl;

    // ИСПРАВЛЕНО: Правильный Realm
    @Value("${keycloak.realm:bank-realm}")
    private String realm;

    // ИСПРАВЛЕНО: Правильный Client ID
    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    public String getKeycloakUrl() { return keycloakUrl; }
    public String getRealm() { return realm; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
    public String getTokenUrl() {
        return keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }
}
