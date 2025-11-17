package com.example.accounts_service.config;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtDecoderConfig {

    // Внедряем весь объект свойств Spring Security
    private final OAuth2ResourceServerProperties properties;

    // Используем конструктор для внедрения, чтобы гарантировать доступность свойств
    public JwtDecoderConfig(OAuth2ResourceServerProperties properties) {
        this.properties = properties;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Получаем адреса из внедренного объекта свойств
        String jwkSetUri = this.properties.getJwt().getJwkSetUri();
        String issuerUri = this.properties.getJwt().getIssuerUri();

        // 1. Создаем декодер, который ходит по ВНУТРЕННЕМУ адресу (jwk-set-uri)
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // 2. Создаем валидатор для ВНЕШНЕГО 'iss' (issuer-uri)
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuerUri);

        // 3. Объединяем валидаторы
        OAuth2TokenValidator<Jwt> withDefaultValidators =
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        JwtValidators.createDefault()
                );

        jwtDecoder.setJwtValidator(withDefaultValidators);

        return jwtDecoder;
    }
}