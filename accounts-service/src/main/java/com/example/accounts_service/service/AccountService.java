package com.example.accounts_service.service;

import com.example.accounts_service.model.Account;
import com.example.accounts_service.model.Currency;
import com.example.accounts_service.model.User;
import com.example.accounts_service.client.ExchangeServiceClient;
import com.example.accounts_service.repository.AccountRepository;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    private final ExchangeServiceClient exchangeServiceClient;


    public AccountService(UserRepository userRepository, AccountRepository accountRepository, KeycloakClient keycloakClient, ExchangeServiceClient exchangeServiceClient) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.keycloakClient = keycloakClient;
        this.exchangeServiceClient = exchangeServiceClient;
    }

    public Account createAccount(String login, Currency currency) {
        User user = userRepository.findByKeycloakId(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (accountRepository.existsByUserAndCurrency(user, currency)) {
            throw new RuntimeException("Счет в валюте " + currency + " уже существует");
        }

        Account account = new Account(user, currency);
        return accountRepository.save(account);
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
    }

    @Transactional
    public void createDefaultAccountsForUser(User user) {
        System.out.println("Создание счетов по умолчанию для пользователя: " + user.getKeycloakId());
        // Создаем счета в основных валютах
        for (Currency currency : Currency.values()) {
            Account account = new Account(user, currency);
            account.setBalance(BigDecimal.ZERO);
            accountRepository.save(account);
        }
        System.out.println("Счета по умолчанию успешно созданы.");
    }

    public Optional<User> findByLogin(String login) {
        String keycloakUserId = keycloakClient.getUserIdByUsername(login);
        return userRepository.findByKeycloakId(keycloakUserId);
    }

    public List<Account> getUserAccountsByLogin(String login) {
        try {
            // Находим пользователя по логину
            Optional<User> userOpt = findByLogin(login);
            if (userOpt.isPresent()) {
                System.out.println("Нашли пользователя");
                User user = userOpt.get();
                System.out.println("User_id: " + user.getId());
                List<Account> accounts = accountRepository.findByUser(user);

                // Гарантируем, что список не содержит null
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
        // Проверка возраста (если нужна)

        User user = userRepository.findByKeycloakId(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return userRepository.save(user);
    }

    @Transactional
    public void transferFunds(
            String fromLogin,
            String toLogin,
            String fromCurrencyCode,
            String toCurrencyCode,
            BigDecimal amount) {

        String fromKeycloakId = keycloakClient.getUserIdByUsername(fromLogin);
        String toKeycloakId = keycloakClient.getUserIdByUsername(toLogin);

        System.out.println("fromKeycloakId: " + fromKeycloakId + "toKeycloakId: " + toKeycloakId);

        // 1. Находим пользователей
        User fromUser = userRepository.findByKeycloakId(fromKeycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Source user not found."));
        User toUser = userRepository.findByKeycloakId(toKeycloakId)
                .orElseThrow(() -> new IllegalArgumentException("Destination user not found."));

        Currency fromCurrency = Currency.valueOf(fromCurrencyCode);
        Currency toCurrency = Currency.valueOf(toCurrencyCode);

        // 2. Находим счета
        Account fromAccount = accountRepository
                .findByUserAndCurrency(fromUser, fromCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found for currency: " + fromCurrencyCode));

        Account toAccount = accountRepository
                .findByUserAndCurrency(toUser, toCurrency)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found for currency: " + toCurrencyCode));

        // 3. Проверяем баланс и выполняем списание
        if (!fromAccount.withdraw(amount)) {
            throw new IllegalArgumentException("Insufficient funds in " + fromCurrencyCode + " account.");
        }
        accountRepository.save(fromAccount); // Сохраняем списание

        // 4. Рассчитываем сумму для зачисления с учетом конвертации
        BigDecimal depositAmount;
        if (fromCurrency.equals(toCurrency)) {
            depositAmount = amount;
        } else {
            // Требуется конвертация через Exchange Service
            depositAmount = exchangeServiceClient.convert(fromCurrencyCode, toCurrencyCode, amount);
        }

        // 5. Зачисляем средства
        toAccount.deposit(depositAmount);
        accountRepository.save(toAccount);
    }
}