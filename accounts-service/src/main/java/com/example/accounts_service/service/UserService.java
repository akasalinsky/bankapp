package com.example.accounts_service.service;

import com.example.accounts_service.model.User;
import com.example.accounts_service.repository.AccountRepository;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AccountService accountService; // <-- Внедряем AccountService


    public UserService(UserRepository userRepository, AccountService accountService, KeycloakClient keycloakClient) {
        this.userRepository = userRepository;
        this.keycloakClient = keycloakClient;
        this.accountService = accountService; // <-- Инициализируем

    }


    @Transactional
    public User registerUser(String login, String password,
                             String firstName, String lastName, LocalDate birthDate) {
        String keycloakId = keycloakClient.createUserInKeycloak(login, password, firstName, lastName, birthDate);

        User newUser = new User(keycloakId);
        newUser.setKeycloakId(keycloakId);

        User savedUser = userRepository.save(newUser);

        accountService.createDefaultAccountsForUser(savedUser);

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
        // Электронная почта
        if (email != null && !email.trim().isEmpty()) {
            userUpdateBody.put("email", email);
            userUpdateBody.put("emailVerified", true);
        }

        // Атрибуты (дата рождения)
        String birthDateString = null;
        if (birthDate != null) {
            birthDateString = birthDate.format(DateTimeFormatter.ISO_DATE);
        }
        Map<String, List<String>> attributes = (Map<String, List<String>>) userUpdateBody.getOrDefault("attributes", new HashMap<>());

        if (birthDateString != null) {
            attributes.put("birthdate", Collections.singletonList(birthDateString));
        } else {
            // Если дата рождения не передана (null), удаляем ее из атрибутов,
            // чтобы Keycloak не использовал старое значение, если пользователь хочет его стереть.
            attributes.remove("birthdate");
        }
        userUpdateBody.put("attributes", attributes);

        // 4. ОТПРАВЛЯЕМ ПОЛНЫЙ ИЗМЕНЕННЫЙ ОБЪЕКТ ОБРАТНО (PUT)
        keycloakClient.updateUserProfile( keycloakUserId, userUpdateBody);    }

    public void changePassword(String login, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("Пароль не может быть пустым");
        }

        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        keycloakClient.updateUserPassword(keycloakUserId, newPassword);
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
}
