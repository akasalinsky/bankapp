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
    //private final String accountsServiceUrl = "http://accounts-service";
    private final String gatewayUrl = "http://gateway";//"http://gateway";
// Через Service Discovery!

    public UserDetailsServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            String userUrl = gatewayUrl + "/api/accounts/" + username;
            java.util.Map<String, Object> userMap = restTemplate.getForObject(userUrl, java.util.Map.class);

            if (userMap != null && userMap.containsKey("login")) {
                String login = (String) userMap.get("login");
                String password = (String) userMap.get("password");

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