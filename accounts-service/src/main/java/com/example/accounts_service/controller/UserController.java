package com.example.accounts_service.controller;

import com.example.accounts_service.model.User;
import com.example.accounts_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam LocalDate birthDate) {
        System.out.println("Получено" + login + password + firstName + lastName + birthDate);


        try {
            System.out.println("Получено" + login + password + firstName + lastName + birthDate);
            User user = userService.registerUser(login, password, firstName, lastName, birthDate);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{login}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable String login,
            @RequestParam String password,
            @RequestParam String confirm_password) {

        try {
            if (!password.equals(confirm_password)) {
                return ResponseEntity.badRequest().body("Пароли не совпадают.");
            }

            userService.changePassword(login, password);
            return ResponseEntity.ok().build();  // Перенаправляем на главную без ошибок

        } catch (Exception e) {
            System.err.println("Ошибка Keycloak при смене пароля для " + login + ": " + e.getMessage());

            // Важно: возвращаем сообщение, которое может быть полезно для отладки
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при смене пароля: " + e.getMessage());        }
    }

    @PostMapping("/{login}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable String login,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) LocalDate birthdate) {
        try {
            userService.updateUserProfile(login, firstName, lastName, email, birthdate);
            return ResponseEntity.ok().build();  // Перенаправляем на главную без ошибок
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();  // Возвращаем на главную с ошибками
        }
    }
}
