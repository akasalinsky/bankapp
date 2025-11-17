Микросервисное банковское приложение

Проект представляет собой микросервисное банковское приложение, разработанное с использованием Spring Boot и современных микросервисных паттернов.

## Архитектура

Приложение состоит из следующих микросервисов:

- **front-ui** - Веб-интерфейс пользователя (порт 8088)
- **accounts-service** - Управление аккаунтами пользователей
- **cash-service** - Операции пополнения и снятия средств
- **transfer-service** - Переводы между счетами
- **notification-service** - Уведомления о операциях
- **gateway** - API Gateway (порт 8080)
- **eureka-server** - Service Discovery (порт 8761)
- **config-server** - Централизованная конфигурация (порт 8888)
- **keycloak** - Сервер авторизации OAuth 2.0 (порт 8085)
- **postgres** - База данных (порт 5432)

## Технологический стек

- **Java 21**
- **Spring Boot 3.x**
- **Spring Cloud** (Gateway, Eureka, Config)
- **Spring Security OAuth2**
- **PostgreSQL 15**
- **Keycloak 26.4.0**
- **Docker & Docker Compose**

## Предварительные требования

- Docker 20.10+
- Docker Compose 2.0+
- Maven 3.6+ (для локальной разработки)
- Java 21 (для локальной разработки)

## Быстрый запуск

1. Клонируйте репозиторий:

git clone <repository-url>
cd my-bank-app
Запустите приложение с помощью Docker Compose:

docker-compose up -d
Дождитесь полного запуска всех сервисов (2-3 минуты)

Откройте приложение в браузере:

http://localhost:8088
Доступ к сервисам
После запуска приложения доступны следующие endpoints:

Сервис	URL	Порт	Описание
Front UI	http://localhost:8088	8088	Веб-интерфейс приложения
API Gateway	http://localhost:8080	8080	Единая точка входа для API
Eureka Server	http://localhost:8761	8761	Dashboard сервисов
Keycloak	http://localhost:8085	8085	Админ-панель авторизации
Config Server	http://localhost:8888	8888	Сервер конфигурации
Настройка Keycloak
Авторизация в Keycloak Admin Console:
URL: http://localhost:8085

Логин: admin
Пароль: admin

Предварительно настроенные клиенты:
front-ui - веб-клиент для пользовательского интерфейса

accounts-service - сервисный клиент для accounts-service

cash-service - сервисный клиент для cash-service

transfer-service - сервисный клиент для transfer-service

Функциональность
Для пользователей:
✅ Регистрация нового аккаунта

✅ Аутентификация по логину/паролю

✅ Просмотр баланса по валютам

✅ Пополнение счета

✅ Снятие средств

✅ Переводы между счетами

✅ Переводы другим пользователям

✅ Изменение профиля пользователя

✅ Смена пароля

Технические возможности:
✅ Service Discovery (Eureka)

✅ API Gateway с маршрутизацией

✅ Централизованная конфигурация

✅ OAuth2 авторизация между сервисами

✅ Health checks для всех сервисов

✅ Circuit breaker и retry механизмы

✅ Логирование операций

Конфигурация
Основные настройки:
База данных: PostgreSQL с отдельными схемами для каждого сервиса

Аутентификация: Keycloak OAuth2 с JWT токенами

Service Discovery: Eureka Server

Конфигурация: Spring Cloud Config Server


Структура проекта
text
my-bank-app/
├── front-ui/                 # Веб-интерфейс
├── accounts-service/         # Сервис аккаунтов
├── cash-service/            # Сервис операций с наличными
├── transfer-service/        # Сервис переводов
├── notification-service/    # Сервис уведомлений
├── gateway/                 # API Gateway
├── eureka-server/           # Service Discovery
├── config-server/           # Сервер конфигурации
└── docker-compose.yml       # Docker Compose конфигурация