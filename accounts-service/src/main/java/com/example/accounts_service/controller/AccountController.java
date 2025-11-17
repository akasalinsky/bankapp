package com.example.accounts_service.controller;

import com.example.accounts_service.model.Account;
import com.example.accounts_service.model.Currency;
import com.example.accounts_service.model.User;
import com.example.accounts_service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.accounts_service.model.Account;
import com.example.accounts_service.service.AccountService;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    private record TransferRequest(
            @JsonProperty("fromLogin") String fromLogin,
            @JsonProperty("toLogin") String toLogin,
            @JsonProperty("fromCurrency") String fromCurrency,
            @JsonProperty("toCurrency") String toCurrency,
            @JsonProperty("amount") BigDecimal amount) {}

    private record BalanceUpdateRequest(
            String keycloakId,
            String currency,
            BigDecimal amount,
            String operationType) {}

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam String login,
            @RequestParam Currency currency) {

        try {
            Account account = accountService.createAccount(login, currency);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{login}")
    public ResponseEntity<User> getUserByLogin(@PathVariable String login) {
        return accountService.findByLogin(login)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{login}/accounts")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable String login) {
        try {
            System.out.println("Ищем счета");
            List<Account> accounts = accountService.getUserAccountsByLogin(login);
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            System.out.println("Ошибка при получении счетов");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{login}/deposit")
    public ResponseEntity<?> deposit(
            @PathVariable String login,
            @RequestParam BigDecimal amount,
            @RequestParam Currency currency) {

        try {
            accountService.deposit(login, amount, currency);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{login}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable String login,
            @RequestParam BigDecimal amount,
            @RequestParam Currency currency) {

        try {
            accountService.withdraw(login, amount, currency);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{keycloakId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable String keycloakId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.getName().equals(keycloakId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Account> accounts = accountService.getUserAccounts(keycloakId);
            String firstName = null;
            String lastName = null;
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                firstName = jwt.getClaimAsString("given_name");
                lastName = jwt.getClaimAsString("family_name");
            }

            Map<String, Object> response = Map.of(
                    "keycloakId", keycloakId,
                    "firstName", firstName != null ? firstName : "Имя",
                    "lastName", lastName != null ? lastName : "Фамилия",
                    "birthdate", "2000-01-01", // Моковая дата рождения
                    "accounts", accounts.stream().map(a -> Map.of(
                            "currency", a.getCurrency().name(),
                            "balance", a.getBalance().toString()
                    )).collect(Collectors.toList())
            );

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
    @PostMapping("/transfer/self")
    public ResponseEntity<String> transferSelf(@RequestBody TransferRequest request) {
        try {
            accountService.transferFunds(
                    request.fromLogin(),
                    request.toLogin(),
                    request.fromCurrency(),
                    request.toCurrency(),
                    request.amount()
            );
            return ResponseEntity.ok("Transfer successful.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    @PostMapping("/transfer/other")
    public ResponseEntity<String> transferOther(@RequestBody TransferRequest request) {
        if (request.fromLogin().equals(request.toLogin())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use /transfer/self endpoint for self transfers.");
        }

        try {
            accountService.transferFunds(
                    request.fromLogin(),
                    request.toLogin(),
                    request.fromCurrency(),
                    request.toCurrency(),
                    request.amount()
            );
            return ResponseEntity.ok("Transfer successful.");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}