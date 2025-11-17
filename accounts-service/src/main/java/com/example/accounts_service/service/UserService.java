package com.example.accounts_service.service;

import com.example.accounts_service.model.User;
import com.example.accounts_service.repository.AccountRepository;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;



@Service
public class UserService {
    private UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    private final AccountService accountService;
    private final RestTemplate restTemplate;
    private final String gatewayUrl = "http://gateway";

    public UserService(UserRepository userRepository, AccountService accountService, KeycloakClient keycloakClient, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.keycloakClient = keycloakClient;
        this.accountService = accountService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public User registerUser(String login, String password,
                             String firstName, String lastName, LocalDate birthDate) {
        String keycloakId = keycloakClient.createUserInKeycloak(login, password, firstName, lastName, birthDate);
        User newUser = new User(keycloakId);
        newUser.setKeycloakId(keycloakId);
        User savedUser = userRepository.save(newUser);
        accountService.createDefaultAccountsForUser(savedUser);

        sendNotification(login, "REGISTRATION", "Аккаунт успешно зарегистрирован");

        return savedUser;
    }
    public Optional<User> findByLogin(String login) {
        return userRepository.findByKeycloakId(login);
    }
    public void updateUserProfile(String login, String firstName, String lastName, String email, LocalDate birthDate) {
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        Map<String, Object> userUpdateBody = keycloakClient.getUserInfo(keycloakUserId);
        if (firstName != null && !firstName.trim().isEmpty()) {
            userUpdateBody.put("firstName", firstName);
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            userUpdateBody.put("lastName", lastName);
        }
        if (email != null && !email.trim().isEmpty()) {
            userUpdateBody.put("email", email);
            userUpdateBody.put("emailVerified", true);
        }
        String birthDateString = null;
        if (birthDate != null) {
            birthDateString = birthDate.format(DateTimeFormatter.ISO_DATE);
        }
        Map<String, List<String>> attributes = (Map<String, List<String>>) userUpdateBody.getOrDefault("attributes", new HashMap<>());

        if (birthDateString != null) {
            attributes.put("birthdate", Collections.singletonList(birthDateString));
        } else {
            attributes.remove("birthdate");
        }
        userUpdateBody.put("attributes", attributes);
        keycloakClient.updateUserProfile( keycloakUserId, userUpdateBody);

        sendNotification(login, "PROFILE_UPDATED", "Профиль пользователя обновлен");

    }

    public void changePassword(String login, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("Пароль не может быть пустым");
        }
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        keycloakClient.updateUserPassword(keycloakUserId, newPassword);

        sendNotification(login, "PASSWORD_CHANGED", "Пароль успешно изменен");
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    private void sendNotification(String login, String type, String message) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(gatewayUrl)
                    .path("/api/notifications")
                    .queryParam("login", login)
                    .queryParam("type", type)
                    .queryParam("message", message)
                    .toUriString();

            restTemplate.postForObject(url, null, String.class);
            System.out.println("✅ User Service → Notification Service: " + message);

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем основную операцию
            System.out.println("❌ User Service → Notification Service failed: " + e.getMessage());
        }
    }
}
