package com.example.front_ui.controller;

import com.example.front_ui.model.ExchangeRate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

import com.example.front_ui.model.CashRequestDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class MainController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.gateway.url}")
    private String gatewayUrl;

    @GetMapping("/")
    public String mainPage(Model model, Authentication authentication) {
        String jwtToken = null;
        String login = "Неизвестен";

        try {
            jwtToken = getJwtToken(authentication);
            model.addAttribute("accounts", List.of());
            model.addAttribute("balanceByCurrency", new HashMap<>());
            model.addAttribute("currency", java.util.List.of(com.example.front_ui.model.Currency.values()));
            model.addAttribute("firstName", "Не указано");
            model.addAttribute("lastName", "Не указана");

            if (jwtToken == null) {
                model.addAttribute("login", "Неавторизованный пользователь");
                return "main";
            }
                String[] parts = jwtToken.split("\\.");
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> claims = mapper.readValue(payload, Map.class);
                String preferredUsername = (String) claims.get("preferred_username");
                if (preferredUsername != null) {
                    login = preferredUsername;
                }

                String firstName = (String) claims.get("given_name");
                String lastName = (String) claims.get("family_name");
                String birthDate = (String) claims.get("birthdate");
                String name = lastName + " " + firstName;

                if (name != null) {
                    model.addAttribute("name", name);
                }
                if (firstName != null) {
                    model.addAttribute("firstName", firstName);
                }
                if (lastName != null) {
                    model.addAttribute("lastName", lastName);
                }
                if (birthDate != null) {
                    model.addAttribute("birthdate", birthDate);
                }

                model.addAttribute("login", login);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwtToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

                try {
                    String accountsUrl = gatewayUrl + "/api/accounts/" + login + "/accounts";
                    ResponseEntity<List> accountsResponse = restTemplate.exchange(
                            accountsUrl, HttpMethod.GET, entity, List.class
                    );

                    if (accountsResponse.getStatusCode().is2xxSuccessful() && accountsResponse.getBody() != null) {
                        List<Map<String, Object>> accounts = accountsResponse.getBody();

                        List<Map<String, Object>> validAccounts = accounts.stream()
                                .filter(account -> account != null)
                                .collect(Collectors.toList());

                        model.addAttribute("accounts", validAccounts);
                        System.out.println("Accounts loaded: " + validAccounts.size() + " accounts");

                        Map<String, BigDecimal> balanceByCurrency = new HashMap<>();
                        for (Map<String, Object> account : validAccounts) {
                            try {
                                if (account.get("currency") != null && account.get("balance") != null) {
                                    String currency = account.get("currency").toString();
                                    BigDecimal balance = new BigDecimal(account.get("balance").toString());
                                    balanceByCurrency.put(currency, balance);
                                }
                            } catch (Exception e) {
                                System.out.println("Error processing account: " + e.getMessage());
                            }
                        }
                        model.addAttribute("balanceByCurrency", balanceByCurrency);
                    } else {
                        System.out.println("Accounts response status: " + accountsResponse.getStatusCode());
                    }
                } catch (Exception e) {
                    System.out.println("Error fetching accounts: " + e.getMessage());
                    e.printStackTrace(); // Добавьте для диагностики
                }

            } catch (Exception e) {
                System.out.println("General error in mainPage: " + e.getMessage());
                model.addAttribute("error", "Failed to load user data: " + e.getMessage());
                e.printStackTrace();
            }
            return "main";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    private String getJwtToken(Authentication authentication) {
        try {
            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

                OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(),
                        oauthToken.getName()
                );

                if (authorizedClient != null) {
                    OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                    return accessToken.getTokenValue();
                }
            }
            return null;

        } catch (Exception e) {
            System.out.println("Failed to get JWT token: " + e.getMessage());
            return null;
        }
    }

    @PostMapping("/user/{login}/cash")
    public String handleCashOperation(
            @PathVariable String login,
            @RequestParam String currency,
            @RequestParam BigDecimal value,
            @RequestParam String action,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Обрабатываем операцию для пользователя: " + login);

            String jwtToken = getJwtToken(authentication);
            if (jwtToken == null) {
                redirectAttributes.addFlashAttribute("passwordErrors", List.of("Ошибка аутентификации, токен не найден"));
                return "redirect:/";
            }

            CashRequestDTO cashRequest = new CashRequestDTO(login, value, currency, action);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            String cashUrl = gatewayUrl + "/api/cash/user/" + login + "/operations";

            HttpEntity<CashRequestDTO> entity = new HttpEntity<>(cashRequest, headers);

            restTemplate.exchange(
                    cashUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return "redirect:/";

        } catch (RuntimeException e) {
            return "redirect:/?error=Ошибка при операции снятия/пополнения";
        }
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String login,
            @RequestParam String password,
            @RequestParam String confirm_password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String birthdate,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            if (!password.equals(confirm_password)) {
                redirectAttributes.addFlashAttribute("error", "Пароли не совпадают");
                return "redirect:/signup";
            }
            if (birthdate == null || birthdate.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Дата рождения обязательна для заполнения");
                return "redirect:/signup";
            }

            LocalDate birthDate;
            try {
                birthDate = LocalDate.parse(birthdate);
            } catch (DateTimeParseException e) {
                redirectAttributes.addFlashAttribute("error", "Неверный формат даты рождения. Используйте формат ГГГГ-ММ-ДД");
                return "redirect:/signup";
            }

            if (birthDate.isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Дата рождения не может быть в будущем");
                return "redirect:/signup";
            }

            LocalDate eighteenYearsAgo = LocalDate.now().minusYears(18);
            if (birthDate.isAfter(eighteenYearsAgo)) {
                redirectAttributes.addFlashAttribute("error", "Регистрация возможна только для лиц старше 18 лет");
                return "redirect:/signup";
            }

            String registerUrl = UriComponentsBuilder.fromUriString(gatewayUrl)
                    .path("/api/users/register")
                    .queryParam("login", login)
                    .queryParam("password", password)
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .queryParam("birthDate", birthdate)
                    .toUriString();

            restTemplate.postForObject(registerUrl, null, String.class);

            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "redirect:/signup";
        }
    }

    @PostMapping("/api/users/{login}/password")
    public String changePassword(
            @PathVariable String login,
            @RequestParam String password,
            @RequestParam String confirm_password,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if (!password.equals(confirm_password)) {
                return "redirect:/?error=Пароли не совпадают";
            }
            String jwtToken = getJwtToken(authentication);
            if (jwtToken == null) {
                redirectAttributes.addFlashAttribute("passwordErrors", List.of("Ошибка аутентификации, токен не найден"));
                return "redirect:/";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            String changePasswordUrl = gatewayUrl + "/api/users/" + login + "/password?";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(changePasswordUrl)
                    .queryParam("password", password)
                    .queryParam("confirm_password", confirm_password);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return "redirect:/";

        } catch (RuntimeException e) {
            return "redirect:/?error=Ошибка при смене пароля";
        }
    }

    @PostMapping("/api/users/{login}/profile")
    public String updateProfile(
            @PathVariable String login,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String birthdate,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            String jwtToken = getJwtToken(authentication);
            if (jwtToken == null) {
                redirectAttributes.addFlashAttribute("passwordErrors", List.of("Ошибка аутентификации, токен не найден"));
                return "redirect:/";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            String updateProfileUrl = gatewayUrl + "/api/users/" + login + "/profile?";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(updateProfileUrl)
                    .queryParam("firstName", firstName)
                    .queryParam("lastName", lastName)
                    .queryParam("birthdate", birthdate);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange( // <-- 5. Использует .exchange()
                    builder.toUriString(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return "redirect:/";

        } catch (RuntimeException e) {
            return "redirect:/?error=Ошибка при обновлении профиля";
        }
    }

    @PostMapping("/user/{login}/transfer")
    public String handleTransfer(
            @PathVariable String login,
            @RequestParam("from_currency") String fromCurrency, // Имя из формы
            @RequestParam("value") BigDecimal value,
            @RequestParam("to_login") String toLogin, // Добавили параметр для логина получателя
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String jwtToken = getJwtToken(authentication);
        if (jwtToken == null) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of("Ошибка аутентификации, токен не найден"));
            return "redirect:/";
        }
        return transferToOther(login, toLogin, fromCurrency, value, jwtToken, redirectAttributes);
    }

    private HttpHeaders createHeaders(String jwtToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    private String transferToSelf(String login, String fromCurrency, String toCurrency, BigDecimal value, String jwtToken, RedirectAttributes redirectAttributes) {
        try {
            HttpHeaders headers = createHeaders(jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> transferRequest = Map.of(
                    "fromLogin", login,
                    "toLogin", login,
                    "fromCurrency", fromCurrency,
                    "toCurrency", toCurrency,
                    "amount", value
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transferRequest, headers);

            restTemplate.exchange(
                    gatewayUrl + "/api/accounts/transfer/self",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            redirectAttributes.addFlashAttribute("transferSuccess", List.of("Перевод себе выполнен успешно."));
            return "redirect:/";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of("Ошибка перевода себе: " + e.getResponseBodyAsString()));
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of("Непредвиденная ошибка: " + e.getMessage()));
            return "redirect:/";
        }
    }

    private String transferToOther(String fromLogin, String toLogin, String fromCurrency, BigDecimal value, String jwtToken, RedirectAttributes redirectAttributes) {
        try {
            HttpHeaders headers = createHeaders(jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> transferRequest = Map.of(
                    "fromLogin", fromLogin, // Логин отправителя
                    "toLogin", toLogin,     // Логин получателя
                    "fromCurrency", fromCurrency, // Валюта отправителя
                    "toCurrency", fromCurrency, // Так как конвертации нет, валюта получателя та же
                    "amount", value
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transferRequest, headers);

            restTemplate.exchange(
                    gatewayUrl + "/api/accounts/transfer/other", // Эндпоинт для перевода другому
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            redirectAttributes.addFlashAttribute("transferOtherSuccess", List.of("Перевод пользователю **" + toLogin + "** на сумму " + value.toPlainString() + " **" + fromCurrency + "** выполнен успешно."));
            return "redirect:/";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of("Ошибка перевода: " + (e.getResponseBodyAsString().isEmpty() ? e.getMessage() : e.getResponseBodyAsString())));
            return "redirect:/user/" + fromLogin; // Лучше редирект обратно на страницу пользователя
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of("Непредвиденная ошибка: " + e.getMessage()));
            return "redirect:/user/" + fromLogin;
        }
    }

    private void callAccountsServiceWithToken(String login, String jwtToken, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String accountsUrl = gatewayUrl + "/api/accounts/" + login;
            ResponseEntity<String> response = restTemplate.exchange(
                    accountsUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("Accounts Service response: " + response.getStatusCode());

        } catch (org.springframework.web.client.RestClientResponseException e) {
            System.out.println("Accounts Service error: " + e.getStatusText());
            System.out.println("Response body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.out.println("Failed to call Accounts Service: " + e.getMessage());
        }
    }
}