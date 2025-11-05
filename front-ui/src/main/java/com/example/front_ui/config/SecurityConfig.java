
package com.example.front_ui.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Импорты, которые нужно добавить
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Внедряем ClientRegistrationRepository, чтобы получить OIDC-провайдера
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    // 2. Создаем бин OidcClientInitiatedLogoutSuccessHandler
    @Bean
    public OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler successHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // 3. Указываем URI, куда Keycloak должен вернуть пользователя ПОСЛЕ выхода
        //    {baseUrl} - это плейсхолдер, который Spring Security заменит на
        //    адрес вашего приложения (например, http://localhost:8080/).
        successHandler.setPostLogoutRedirectUri("{baseUrl}/");

        return successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler) // <-- 4. Внедряем наш хэндлер
            throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/signup", "/register", "/css/**", "/js/**", "/error**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                // 5. Добавляем конфигурацию logout
                .logout(logout -> logout
                        // Используем наш кастомный OIDC-хэндлер
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                        // Разрешаем доступ к эндпоинту /logout всем
                        .permitAll()
                );

        return http.build();
    }
}
