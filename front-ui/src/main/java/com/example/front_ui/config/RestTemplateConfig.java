package com.example.front_ui.config;

// import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    //@LoadBalanced
    @Bean
    // @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    // @LoadBalanced  // для вызовов других микросервисов через Eureka
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    @Bean  // для внешних вызовов (Keycloak)
    public RestTemplate simpleRestTemplate() {
        return new RestTemplate();
    }
}