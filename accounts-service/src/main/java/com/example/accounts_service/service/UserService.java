package com.example.accounts_service.service;

import com.example.accounts_service.model.User;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User registerUser(String login, String password, String fullName, LocalDate birthDate){
        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("Пользователь с логином " + login + " уже существует");
        }

        if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new RuntimeException("Пользователь должен быть старше 18 лет");
        }

        User user = new User(login, password, fullName, birthDate);
        return userRepository.save(user);

    }
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public User updateUserProfile(Long userId, String fullName, LocalDate birthDate) {
        // Проверка возраста
        if (birthDate.isAfter(LocalDate.now().minusYears(18))) {
            throw new RuntimeException("Пользователь должен быть старше 18 лет");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setFullName(fullName);
        user.setBirthDate(birthDate);

        return userRepository.save(user);
    }

    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("Пароль не может быть пустым");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setPassword(newPassword);
        userRepository.save(user);
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
