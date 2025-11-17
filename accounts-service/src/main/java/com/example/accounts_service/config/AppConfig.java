package com.example.accounts_service.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class AppConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Установка таймаута подключения (connection timeout)
        int connectTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
        factory.setConnectTimeout(connectTimeoutMs);

        // Установка таймаута чтения (read timeout)
        int readTimeoutMs = (int) Duration.ofSeconds(5).toMillis();
        factory.setReadTimeout(readTimeoutMs);

        // Применяем настройки к RestTemplate
        return builder
                // Устанавливаем нашу настроенную фабрику
                .requestFactory(() -> factory)
                .build();
    }

    @Bean
    public RestTemplate simpleRestTemplate(RestTemplateBuilder builder) {
        // Тот же таймаут, но БЕЗ @LoadBalanced
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}
