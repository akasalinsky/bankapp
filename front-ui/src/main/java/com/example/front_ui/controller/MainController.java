package com.example.front_ui.controller;

import com.example.front_ui.model.ExchangeRate;
import jakarta.servlet.http.HttpServletRequest;
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

    private final String gatewayUrl = "http://gateway";
    private final String accountsServiceUrl = "http://accounts-service";
    private final String cashServiceUrl = "http://cash-service";
    private final String exchangeServiceUrl = "http://exchange-service";

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

                System.out.println("firstName " + firstName + "lastName " + lastName + "birthDate " + birthDate);

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
                System.out.println("Profile data loaded from JWT successfully.");

                model.addAttribute("login", login);

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(jwtToken);
                HttpEntity<String> entity = new HttpEntity<>(headers);

            /*try {
                // Получаем курсы валют из Exchange Service
                String exchangeUrl = exchangeServiceUrl + "/api/exchange/rates";
                List<ExchangeRate> rates = restTemplate.getForObject(exchangeUrl, List.class);
                model.addAttribute("exchangeRates", rates);

            } catch (Exception e) {
                model.addAttribute("exchangeRates", java.util.List.of());
            }*/

                try {
                    String accountsUrl = accountsServiceUrl + "/api/accounts/" + login + "/accounts";
                    System.out.println("Fetching accounts from: " + accountsUrl);

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

    private String getJwtToken(Authentication authentication) {
        try {
            System.out.println("Authentication type: " + authentication.getClass().getName());
            System.out.println("Authentication details: " + authentication.getDetails());
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
            RedirectAttributes redirectAttributes) {  // Используйте RedirectAttributes

        try {
            System.out.println("Обрабатываем операцию для пользователя: " + login);

            String jwtToken = getJwtToken(authentication);
            if (jwtToken == null) {
                redirectAttributes.addFlashAttribute("passwordErrors", List.of("Ошибка аутентификации, токен не найден"));
                return "redirect:/";
            }

            // Создаем CashRequest
            CashRequestDTO cashRequest = new CashRequestDTO(login, value, currency, action);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            String cashUrl = cashServiceUrl + "/api/cash/user/" + login + "/operations";

            HttpEntity<CashRequestDTO> entity = new HttpEntity<>(cashRequest, headers);

            // 4. Используем restTemplate.exchange для отправки POST-запроса с телом
            restTemplate.exchange(
                    cashUrl, // URL без параметров запроса
                    HttpMethod.POST,
                    entity, // Передаем HttpEntity с объектом CashRequestDTO
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
            HttpServletRequest request) {

        try {
            if (!password.equals(confirm_password)) {
                model.addAttribute("error", "Пароли не совпадают");
                return "signup";
            }

            String registerUrl = UriComponentsBuilder.fromUriString(accountsServiceUrl)
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
            model.addAttribute("error", "Ошибка регистрации: " + e.getMessage());
            return "signup";
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

            // 3. Создаем заголовки с токеном
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);

            String changePasswordUrl = accountsServiceUrl + "/api/users/" + login + "/password?";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(changePasswordUrl)
                    .queryParam("password", password)
                    .queryParam("confirm_password", confirm_password);

            // 5. Создаем HttpEntity (тело пустое, т.к. параметры в URL)
            HttpEntity<String> entity = new HttpEntity<>(headers);

            restTemplate.exchange( // <-- 5. Использует .exchange()
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

            String updateProfileUrl = accountsServiceUrl + "/api/users/" + login + "/profile?";

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
            @RequestParam("from_currency") String fromCurrency,
            @RequestParam("to_currency") String toCurrency,
            @RequestParam("value") BigDecimal value,
            @RequestParam("to_login") String toLogin,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String jwtToken = getJwtToken(authentication);
        if (jwtToken == null) {
            redirectAttributes.addFlashAttribute("transferErrors", List.of("Ошибка аутентификации, токен не найден"));
            return "redirect:/";
        }

        // Внутренний перевод
        if (login.equals(toLogin)) {
            return transferToSelf(login, fromCurrency, toCurrency, value, jwtToken, redirectAttributes);
        } else {
            // Перевод другому
            return transferToOther(login, toLogin, fromCurrency, toCurrency, value, jwtToken, redirectAttributes);
        }
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

            // Тело запроса для перевода себе
            Map<String, Object> transferRequest = Map.of(
                    "fromLogin", login,
                    "toLogin", login,
                    "fromCurrency", fromCurrency,
                    "toCurrency", toCurrency,
                    "amount", value
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transferRequest, headers);

            restTemplate.exchange(
                    accountsServiceUrl + "/api/accounts/transfer/self",
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

    private String transferToOther(String fromLogin, String toLogin, String fromCurrency, String toCurrency, BigDecimal value, String jwtToken, RedirectAttributes redirectAttributes) {
        try {
            HttpHeaders headers = createHeaders(jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Тело запроса для перевода другому
            Map<String, Object> transferRequest = Map.of(
                    "fromKeycloakId", fromLogin,
                    "toKeycloakId", toLogin,
                    "fromCurrency", fromCurrency,
                    "toCurrency", toCurrency,
                    "amount", value
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(transferRequest, headers);

            restTemplate.exchange(
                    accountsServiceUrl + "/api/accounts/transfer/other", // Эндпоинт для перевода другому
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            redirectAttributes.addFlashAttribute("transferOtherSuccess", List.of("Перевод другому пользователю выполнен успешно."));
            return "redirect:/";

        } catch (HttpClientErrorException e) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of("Ошибка перевода другому: " + e.getResponseBodyAsString()));
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("transferOtherErrors", List.of("Непредвиденная ошибка: " + e.getMessage()));
            return "redirect:/";
        }
    }

    private void callAccountsServiceWithToken(String login, String jwtToken, Model model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(jwtToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String accountsUrl = accountsServiceUrl + "/api/accounts/" + login;
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
    @GetMapping("/api/exchange/rates")
    public ResponseEntity<List<ExchangeRate>> getExchangeRates(
            Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Получаем JWT токен (если нужно для авторизации в Exchange Service)
            String jwtToken = getJwtToken(authentication);

            HttpHeaders headers = new HttpHeaders();
            if (jwtToken != null) {
                headers.setBearerAuth(jwtToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Вызываем Exchange Service через Gateway
            String exchangeUrl = exchangeServiceUrl + "/api/exchange/rates";
            ResponseEntity<List> response = restTemplate.exchange(
                    exchangeUrl, HttpMethod.GET, entity, List.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}