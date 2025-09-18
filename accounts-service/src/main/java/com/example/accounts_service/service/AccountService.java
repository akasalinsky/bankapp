package com.example.accounts_service.service;

import com.example.accounts_service.model.Account;
import com.example.accounts_service.model.Currency;
import com.example.accounts_service.model.User;
import com.example.accounts_service.repository.AccountRepository;
import com.example.accounts_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Account createAccount(Long userId, Currency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка, что счет в этой валюте еще не существует
        if (accountRepository.existsByUserAndCurrency(user, currency)) {
            throw new RuntimeException("Счет в валюте " + currency + " уже существует");
        }

        Account account = new Account(user, currency);
        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return accountRepository.findByUser(user);
    }

    public Optional<Account> getAccount(Long accountId) {
        return accountRepository.findById(accountId);
    }

    public Account updateAccount(Long accountId, Currency currency) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        account.setCurrency(currency);
        return accountRepository.save(account);
    }

    public void deleteAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new RuntimeException("Счет не найден");
        }
        accountRepository.deleteById(accountId);
    }

    public void deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма должна быть положительной");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        account.deposit(amount);
        accountRepository.save(account);
    }

    public void withdraw(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма должна быть положительной");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Счет не найден"));

        if (!account.withdraw(amount)) {
            throw new RuntimeException("Недостаточно средств на счете");
        }

        accountRepository.save(account);
    }
}