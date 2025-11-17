package com.example.accounts_service.service;

import com.example.accounts_service.model.Account;
import com.example.accounts_service.model.Currency;
import com.example.accounts_service.model.User;
import com.example.accounts_service.repository.AccountRepository;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private final KeycloakClient keycloakClient;
    private final RestTemplate restTemplate;
    private final String gatewayUrl = "http://gateway";

    public AccountService(UserRepository userRepository, AccountRepository accountRepository, KeycloakClient keycloakClient, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.keycloakClient = keycloakClient;
        this.restTemplate = restTemplate;
    }

    public Account createAccount(String login, Currency currency) {
        User user = userRepository.findByKeycloakId(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (accountRepository.existsByUserAndCurrency(user, currency)) {
            throw new RuntimeException("Счет в валюте " + currency + " уже существует");
        }
        Account account = new Account(user, currency);
        Account savedAccount = accountRepository.save(account);

        sendNotification(login, "ACCOUNT_CREATED", "Создан счет в валюте " + currency);

        return savedAccount;
    }

    public List<Account> getUserAccounts(String login) {
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        User user = userRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Account> accounts = accountRepository.findByUser(user);
        return accounts.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public void deposit(String login, BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма должна быть положительной");
        }
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        User user = userRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
        Account account = accountRepository.findByUserAndCurrency(user, currency)
                .orElseGet(() -> {
                    System.out.println("Счет не найден, создаем новый для валюты: " + currency);
                    Account newAccount = new Account(user, currency);
                    newAccount.setBalance(BigDecimal.ZERO);
                    return accountRepository.save(newAccount);
                });

        account.deposit(amount);
        accountRepository.save(account);
        System.out.println("Успешное пополнение: " + login + ", валюта: " + currency + ", сумма: " + amount);
        sendNotification(login, "DEPOSIT", "Счет пополнен на " + amount + " " + currency);

    }

    public void withdraw(String login, BigDecimal amount, Currency currency) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма должна быть положительной");
        }
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        User user = userRepository.findByKeycloakId(keycloakUserId)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
        Account account = accountRepository.findByUserAndCurrency(user, currency)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));
        if (!account.withdraw(amount)) {
            throw new RuntimeException("Недостаточно средств на счете");
        }
        accountRepository.save(account);

        sendNotification(login, "WITHDRAW", "Со счета снято " + amount + " " + currency);

    }

    @Transactional
    public void createDefaultAccountsForUser(User user) {
        System.out.println("Создание счетов по умолчанию для пользователя: " + user.getKeycloakId());
        for (Currency currency : Currency.values()) {
            Account account = new Account(user, currency);
            account.setBalance(BigDecimal.ZERO);
            accountRepository.save(account);
        }
        System.out.println("Счета по умолчанию успешно созданы.");

        sendNotification(user.getKeycloakId(), "ACCOUNTS_CREATED", "Созданы счета по умолчанию во всех валютах");

    }

    public Optional<User> findByLogin(String login) {
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        return userRepository.findByKeycloakId(keycloakUserId);
    }

    public List<Account> getUserAccountsByLogin(String login) {
        try {
            Optional<User> userOpt = findByLogin(login);
            if (userOpt.isPresent()) {
                System.out.println("Нашли пользователя");
                User user = userOpt.get();
                System.out.println("User_id: " + user.getId());
                List<Account> accounts = accountRepository.findByUser(user);
                return accounts.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
            System.out.println("Не нашли пользователя");
            return List.of();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения счетов пользователя: " + e.getMessage(), e);
        }
    }


    @Transactional
    public User updateUserProfile(String login, String name, LocalDate birthDate) {
        User user = userRepository.findByKeycloakId(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        User savedUser = userRepository.save(user);

        sendNotification(login, "PROFILE_UPDATED", "Профиль пользователя обновлен");

        return savedUser;
    }

    @Transactional
    public void transferFunds(
            String fromLogin,
            String toLogin,
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal amount) {

        String fromKeycloakId = keycloakClient.getUserIdByUsername(fromLogin);
        if (fromKeycloakId == null || fromKeycloakId.trim().isEmpty()) {
            throw new IllegalArgumentException("Source user (Keycloak ID) not found for login: " + fromLogin);
        }
        String toKeycloakId = keycloakClient.getUserIdByUsername(toLogin);
        if (toKeycloakId == null || toKeycloakId.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination user (Keycloak ID) not found for login: " + toLogin);
        }

        System.out.println("fromKeycloakId: " + fromKeycloakId + " toKeycloakId: " + toKeycloakId);

        if (!fromCurrencyCode.equals(toCurrencyCode)) {
            throw new IllegalArgumentException("Cross-currency transfers are not supported by this service.");
        }
        User fromUser = userRepository.findByKeycloakId(fromKeycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Source user not found."));
        User toUser = userRepository.findByKeycloakId(toKeycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Destination user not found."));

        Currency currency = Currency.valueOf(fromCurrencyCode);

        Account fromAccount = accountRepository
                .findByUserAndCurrency(fromUser, currency)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found for currency: " + fromCurrencyCode));

        Account toAccount = accountRepository
                .findByUserAndCurrency(toUser, currency)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found for currency: " + toCurrencyCode));

        if (!fromAccount.withdraw(amount)) {
            throw new IllegalArgumentException("Insufficient funds in " + fromCurrencyCode + " account.");
        }
        accountRepository.save(fromAccount);
        toAccount.deposit(amount);
        accountRepository.save(toAccount);

        sendNotification(fromLogin, "TRANSFER", "Перевод на сумму " + amount + " " + currency + " пользователю " + toLogin);
        sendNotification(toLogin, "TRANSFER", "Получен перевод на сумму " + amount + " " + currency + " от пользователя " + fromLogin);
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
            System.out.println("✅ Account Service → Notification Service: " + message);

        } catch (Exception e) {
            // Логируем ошибку, но не прерываем основную операцию
            System.out.println("❌ Account Service → Notification Service failed: " + e.getMessage());
        }
    }
}