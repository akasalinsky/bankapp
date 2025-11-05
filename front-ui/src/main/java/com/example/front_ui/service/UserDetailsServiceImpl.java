package com.example.front_ui.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final RestTemplate restTemplate;
    private final String accountsServiceUrl = "http://accounts-service";  // Через Service Discovery!

    public UserDetailsServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Вызываем Accounts Service через Gateway для получения пользователя
            String userUrl = accountsServiceUrl + "/api/accounts/" + username;

            // Получаем пользователя из Accounts Service
            // Пока используем Map для десериализации JSON
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> userMap = restTemplate.getForObject(userUrl, java.util.Map.class);

            if (userMap != null && userMap.containsKey("login")) {
                String login = (String) userMap.get("login");
                String password = (String) userMap.get("password");

                // Создаем UserDetails с данными из Accounts Service
                return User.withUsername(login)
                        .password("{noop}" + password)  // {noop} = без шифрования
                        .authorities("USER")
                        .build();
            }

            throw new UsernameNotFoundException("User not found: " + username);

        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}