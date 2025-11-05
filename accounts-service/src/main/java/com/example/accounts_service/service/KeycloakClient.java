package com.example.accounts_service.service;

import com.example.accounts_service.config.KeycloakConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

@Service
public class KeycloakClient {

    private final RestTemplate restTemplate;
    private final KeycloakConfig keycloakConfig;

    public KeycloakClient(RestTemplate restTemplate, KeycloakConfig keycloakConfig) {
        this.restTemplate = restTemplate;
        this.keycloakConfig = keycloakConfig;
    }

    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        System.out.println("Вот оно " + keycloakConfig.getClientId() + "и оно " + keycloakConfig.getClientSecret());

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", keycloakConfig.getClientId()); // Ваш bank-client
        map.add("client_secret", keycloakConfig.getClientSecret()); // Ваш secret

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    keycloakConfig.getTokenUrl(),
                    request,
                    Map.class
            );

            if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
                throw new RuntimeException("Не удалось получить access_token от Keycloak");
            }
            return (String) response.getBody().get("access_token");
        }
        catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("HTTP ошибка при получении административного токена Keycloak: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Ошибка при получении токена:");
            e.printStackTrace();
            throw new RuntimeException("Ошибка при получении административного токена Keycloak: " + e.getMessage(), e);
        }
        }


    public String createUserInKeycloak(String login, String password, String firstName, String lastName, LocalDate birthDate) {

        String adminToken = getAdminToken();

        Map<String, Object> userPayload = Map.of(
                "username", login,
                "email", login + "@bank.com",
                "firstName", firstName,
                "lastName", lastName,
                //"birthDate", birthDate,
                "enabled", true,
                "attributes", Map.of("birthdate", List.of(birthDate.toString())),
                "credentials", Collections.singletonList(
                        Map.of(
                                "type", "password",
                                "value", password,
                                "temporary", false // Делаем пароль постоянным
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userPayload, headers);

        String usersUrl = keycloakConfig.getKeycloakUrl() + "/admin/realms/" + keycloakConfig.getRealm() + "/users";

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    usersUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                System.out.println("Статус: 201 CREATED (пользователь создан)");
                URI location = response.getHeaders().getLocation();
                if (location == null) {
                    throw new RuntimeException("Keycloak вернул 201, но заголовок Location отсутствует. Невозможно получить ID.");
                }
                // Извлекаем UUID пользователя из Location
                String path = location.getPath();
                String keycloakId = path.substring(path.lastIndexOf('/') + 1);
                System.out.println("Keycloak ID пользователя: " + keycloakId);
                return keycloakId;

            } else {
                throw new RuntimeException("Keycloak вернул неожиданный статус: " + response.getStatusCode());
            }

        } catch (HttpClientErrorException e) {
            String errorBody = Objects.toString(e.getResponseBodyAsString(), "N/A");
            System.err.println("Keycloak HTTP Error: " + e.getStatusCode() + ", Body: " + errorBody);

            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new RuntimeException("Пользователь с таким логином уже существует в Keycloak.", e);
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Некорректный запрос к Keycloak API: " + errorBody, e);
            }
            throw new RuntimeException("Keycloak API Error: " + errorBody, e);
        } catch (Exception e) {
            System.err.println("Непредвиденная ошибка при создании пользователя:");
            e.printStackTrace();
            throw new RuntimeException("Непредвиденная ошибка при создании пользователя в Keycloak: " + e.getMessage(), e);
        }
    }
    public void updateUserPassword(String keycloakUserId, String newPassword) {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String keycloakAdminUrl = keycloakConfig.getKeycloakUrl();
        String realm = keycloakConfig.getRealm();


        // Тело запроса для сброса пароля (Ожидается Map, содержащая тип кредов)
        Map<String, Object> passwordCredential = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false // Делаем пароль постоянным
        );

        // Keycloak Admin API для reset-password требует, чтобы тело было объектом CredentialRepresentation,
        // но в Spring и RestTemplate часто требуется List для этого эндпоинта.
        // Мы используем List<Map> для максимальной совместимости.
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(passwordCredential, headers);

        // URL: {adminUrl}/admin/realms/{realm}/users/{id}/reset-password
        String url = UriComponentsBuilder.fromHttpUrl(keycloakAdminUrl)
                .pathSegment("admin", "realms", realm, "users", keycloakUserId, "reset-password")
                .toUriString();

        try {
            // Выполняем PUT запрос. Keycloak вернет 204 No Content при успехе.
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            // 400 Bad Request, 404 Not Found и другие HTTP ошибки
            throw new RuntimeException("Ошибка Keycloak API при смене пароля (HTTP " + e.getRawStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка при смене пароля: " + e.getMessage(), e);
        }
    }

    /*public void updateUserProfile(String keycloakUserId, String login, String firstName, String lastName, String email, LocalDate birthDate) {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String keycloakAdminUrl = keycloakConfig.getKeycloakUrl();
        String realm = keycloakConfig.getRealm();

        Map<String, Object> userUpdateBody = new HashMap<>();

        // Keycloak требует, чтобы username (login) присутствовал при обновлении
        userUpdateBody.put("username", login);
        userUpdateBody.put("firstName", firstName);
        userUpdateBody.put("lastName", lastName);

        // 2. Добавляем кастомный атрибут birthdate
        userUpdateBody.put("attributes", Map.of("birthdate", List.of(birthDate.toString())));

        // 3. Создаем HttpEntity с обновленным телом
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userUpdateBody, headers);

        // 4. ИСПОЛЬЗУЕМ ПРАВИЛЬНЫЙ URL ДЛЯ ОБНОВЛЕНИЯ ПОЛЬЗОВАТЕЛЯ (PUT на базовый ресурс)
        String url = UriComponentsBuilder.fromHttpUrl(keycloakAdminUrl)
                .pathSegment("admin", "realms", realm, "users", keycloakUserId) // <--- ПРАВИЛЬНЫЙ URL
                .toUriString();

        try {
            // Выполняем PUT запрос. Keycloak вернет 204 No Content при успехе.
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            // 400 Bad Request, 404 Not Found и другие HTTP ошибки
            throw new RuntimeException("Ошибка Keycloak API при смене данных пользователя (HTTP " + e.getRawStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка при смене данных пользователя: " + e.getMessage(), e);
        }
    }*/
    public void updateUserProfile(String keycloakUserId, Map<String, Object> userUpdateBody) {
        // Получение токена и заголовков остается прежним
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String keycloakAdminUrl = keycloakConfig.getKeycloakUrl();
        String realm = keycloakConfig.getRealm();

        // 1. Формируем тело запроса для обновления
        // Keycloak требует полный объект пользователя. Используем HashMap для удобного добавления/проверки полей.
        //Map<String, Object> userUpdateBody = new HashMap<>();

        // Keycloak требует, чтобы username (login) присутствовал при обновлении
        //userUpdateBody.put("username", login);

        // 2. Добавляем поля только если они не пустые (решение проблемы 400, если поле не указано)
      /*  if (firstName != null && !firstName.trim().isEmpty()) {
            userUpdateBody.put("firstName", firstName);
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            userUpdateBody.put("lastName", lastName);
        }
        // 3. ОБЯЗАТЕЛЬНО отправляем email, чтобы Keycloak его не сбросил
        if (email != null && !email.trim().isEmpty()) {
            userUpdateBody.put("email", email);
            userUpdateBody.put("emailVerified", true); // Обычно при ручном обновлении делаем подтвержденным
        }

        // 4. Обрабатываем кастомный атрибут birthdate
        if (birthDateString != null && !birthDateString.trim().isEmpty()) {
            userUpdateBody.put("attributes", Map.of("birthdate", List.of(birthDateString)));
        }*/

        // 5. Создаем HttpEntity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(userUpdateBody, headers);

        // 6. ПРАВИЛЬНЫЙ URL: PUT на базовый ресурс пользователя
        String url = UriComponentsBuilder.fromHttpUrl(keycloakAdminUrl)
                .pathSegment("admin", "realms", realm, "users", keycloakUserId)
                .toUriString();

        try {
            // Выполняем PUT запрос. Keycloak вернет 204 No Content при успехе.
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            // ... (Обработка ошибок)
            System.err.println("Keycloak Response Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("Ошибка Keycloak API при смене данных пользователя (HTTP " + e.getRawStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка при смене данных пользователя: " + e.getMessage(), e);
        }
    }


    public String getUserIdByUsername(String username) {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        String keycloakAdminUrl = keycloakConfig.getKeycloakUrl();
        String realm = keycloakConfig.getRealm();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = UriComponentsBuilder.fromHttpUrl(keycloakAdminUrl)
                .pathSegment("admin", "realms", realm, "users")
                .queryParam("username", username)
                .queryParam("exact", "true") // Точное совпадение
                .toUriString();

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

            List<Map<String, Object>> users = response.getBody();
            if (users == null || users.isEmpty()) {
                throw new RuntimeException("Пользователь Keycloak с логином '" + username + "' не найден.");
            }
            if (users.size() > 1) {
                throw new RuntimeException("Найдено несколько пользователей Keycloak с логином '" + username + "'.");
            }

            return (String) users.get(0).get("id");

        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске пользователя Keycloak: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getUserInfo(String keycloakUserId) {
        String adminToken = getAdminToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String keycloakAdminUrl = keycloakConfig.getKeycloakUrl();
        String realm = keycloakConfig.getRealm();

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // URL: {adminUrl}/admin/realms/{realm}/users/{id}
        String url = UriComponentsBuilder.fromHttpUrl(keycloakAdminUrl)
                .pathSegment("admin", "realms", realm, "users", keycloakUserId)
                .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getBody() == null) {
                throw new RuntimeException("Keycloak вернул пустое тело при запросе информации о пользователе.");
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка Keycloak API при получении данных (HTTP " + e.getRawStatusCode() + "): " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("Неизвестная ошибка при получении данных пользователя: " + e.getMessage(), e);
        }
    }
}
