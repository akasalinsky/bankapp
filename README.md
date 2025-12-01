# Микросервисное банковское приложение Bank System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.28+-blue.svg)](https://kubernetes.io/)
[![Helm](https://img.shields.io/badge/Helm-3.x-0F1689.svg)](https://helm.sh/)

Проект представляет собой микросервисное банковское приложение, разработанное с использованием Spring Boot, Kubernetes и современных микросервисных паттернов.

## 📋 Оглавление

- [Архитектура](#архитектура)
- [Технологический стек](#технологический-стек)
- [Системные требования](#системные-требования)
- [Развертывание в Kubernetes](#развертывание-в-kubernetes)
- [Локальная разработка](#локальная-разработка)
- [Jenkins CI/CD](#jenkins-cicd)
- [Функциональность](#функциональность)
- [Troubleshooting](#troubleshooting)

## 🏗 Архитектура

Приложение состоит из следующих микросервисов:

### Микросервисы
- **front-ui** - Веб-интерфейс пользователя (порт 8088)
- **accounts-service** - Управление аккаунтами и счетами (порт 8081)
- **cash-service** - Операции пополнения и снятия средств (порт 8082)
- **transfer-service** - Переводы между счетами (порт 8083)
- **exchange-service** - Конвертация валют (порт 8084)
- **exchange-generator** - Генератор курсов валют
- **notification-service** - Уведомления о операциях (порт 8086)

### Инфраструктура
- **oauth-server** (Keycloak) - Сервер авторизации OAuth 2.0 (порт 8080)
- **postgres-db** - База данных PostgreSQL (порт 5432)
- **ingress** - Gateway API для внешнего доступа

## 🛠 Технологический стек

### Backend
- **Java 21**
- **Spring Boot 3.x**
- **Spring Security OAuth2 Resource Server**
- **Spring Data JPA**
- **PostgreSQL 15**

### Infrastructure
- **Kubernetes 1.28+** - оркестрация контейнеров
- **Helm 3.x** - пакетный менеджер для Kubernetes
- **Docker** - контейнеризация
- **Keycloak 26.4.0** - OAuth 2.0 / OpenID Connect

### CI/CD
- **Jenkins** - автоматизация сборки и развертывания
- **Maven 3.9+** - сборка проектов
- **Git** - система контроля версий

## 📦 Системные требования

### Для развертывания в Kubernetes

| Компонент | Минимальная версия | Рекомендуемая версия |
|-----------|-------------------|---------------------|
| Kubernetes | 1.24+ | 1.28+ |
| Helm | 3.10+ | 3.13+ |
| kubectl | 1.24+ | 1.28+ |
| Docker | 20.10+ | 24.0+ |

### Ресурсы кластера

| Окружение | CPU | RAM | Диск |
|-----------|-----|-----|------|
| DEV | 4 cores | 8 GB | 20 GB |
| TEST | 8 cores | 16 GB | 50 GB |
| PROD | 16 cores | 32 GB | 100 GB |

### Для локальной разработки

- **Java 21** (OpenJDK или Oracle JDK)
- **Maven 3.9+**
- **Docker Desktop** (для Windows/Mac) или **Docker Engine** (для Linux)
- **IDE**: IntelliJ IDEA, Eclipse или VS Code

## 🚀 Развертывание в Kubernetes

### Вариант 1: Быстрое развертывание (по умолчанию)

```bash
# 1. Клонировать репозиторий
git clone <repository-url>
cd bankapp

# 2. Собрать Docker образы
cd bank-umbrella
.\build_images.bat  # Windows
# или
./build_images.sh   # Linux/Mac

# 3. Развернуть приложение
cd bank-umbrella
helm install bank-system . --create-namespace --namespace default --timeout 10m

# 4. Проверить статус
kubectl get pods
```

### Вариант 2: Развертывание в конкретное окружение

#### Development

```bash
# Windows
cd bank-umbrella
deploy-dev.bat

# Linux/Mac
cd bank-umbrella
chmod +x deploy-dev.sh
./deploy-dev.sh
```

#### Testing

```bash
# Windows
cd bank-umbrella
deploy-test.bat

# Linux/Mac
cd bank-umbrella
chmod +x deploy-test.sh
./deploy-test.sh
```

#### Production

```bash
# Windows
cd bank-umbrella
deploy-prod.bat  # Запросит подтверждение

# Linux/Mac
cd bank-umbrella
chmod +x deploy-prod.sh
./deploy-prod.sh  # Запросит подтверждение
```

### Helm Charts - Подробное использование

#### Просмотр доступных значений

```bash
helm show values bank-umbrella
```

#### Кастомизация при установке

```bash
# Установка с кастомными значениями
helm install bank-system bank-umbrella \
  --namespace prod \
  --create-namespace \
  --set accounts-service.replicaCount=3 \
  --set postgres-db.persistence.size=20Gi \
  --timeout 10m
```

#### Обновление релиза

```bash
# Обновить существующий релиз
helm upgrade bank-system bank-umbrella \
  --namespace prod \
  --values bank-umbrella/values-prod.yaml \
  --timeout 10m
```

#### Управление релизами

```bash
# Список релизов
helm list --all-namespaces

# История релиза
helm history bank-system -n prod

# Откат к предыдущей версии
helm rollback bank-system -n prod

# Удаление релиза
helm uninstall bank-system -n prod
```

#### Запуск Helm тестов

```bash
# Запуск тестов для проверки работоспособности
helm test bank-system -n dev
helm test bank-system -n test
helm test bank-system -n prod
```

### Примеры команд для разных окружений

#### Development Environment

```bash
# Создание namespace
kubectl create namespace dev

# Развертывание
helm upgrade --install bank-system bank-umbrella \
  --namespace dev \
  --values bank-umbrella/values-dev.yaml \
  --create-namespace \
  --timeout 10m

# Просмотр подов
kubectl get pods -n dev

# Логи specific service
kubectl logs -n dev -l app.kubernetes.io/name=accounts-service -f

# Port-forward к front-ui
kubectl port-forward -n dev svc/bank-system-front-ui 8088:8088
```

#### Test Environment

```bash
# Развертывание с конкретным image tag (CI/CD)
helm upgrade --install bank-system bank-umbrella \
  --namespace test \
  --values bank-umbrella/values-test.yaml \
  --set accounts-service.image.tag=build-123 \
  --set cash-service.image.tag=build-123 \
  --set transfer-service.image.tag=build-123 \
  --create-namespace \
  --timeout 10m

# Запуск тестов
helm test bank-system -n test

# Просмотр событий
kubectl get events -n test --sort-by='.lastTimestamp'
```

#### Production Environment

```bash
# Развертывание stable версии
helm upgrade --install bank-system bank-umbrella \
  --namespace prod \
  --values bank-umbrella/values-prod.yaml \
  --set accounts-service.image.tag=v2.0 \
  --set cash-service.image.tag=v2.0 \
  --set transfer-service.image.tag=v2.0 \
  --set exchange-service.image.tag=v2.0 \
  --set notification-service.image.tag=v2.0 \
  --set front-ui.image.tag=v2.0 \
  --create-namespace \
  --timeout 15m

# Мониторинг развертывания
kubectl rollout status deployment/bank-system-accounts-service -n prod

# Масштабирование
kubectl scale deployment bank-system-accounts-service --replicas=5 -n prod
```

## 🔄 Jenkins CI/CD

### Настройка Jenkins

#### Предварительные требования в Jenkins

1. **Плагины:**
   - Git Plugin
   - Docker Pipeline
   - Kubernetes CLI
   - Pipeline

2. **Credentials:**
   - GitHub credentials (ID: `github-credentials`)
   - Kubernetes config (ID: `kubeconfig`)
   - Docker registry (ID: `docker-registry`)

#### Создание Pipeline Jobs

**Для отдельного микросервиса (например, accounts-service):**

1. Jenkins → New Item → Multibranch Pipeline
2. Name: `accounts-service`
3. Branch Sources: Git
   - Repository URL: `https://github.com/YOUR_USERNAME/bankapp.git`
   - Credentials: `github-credentials`
4. Build Configuration:
   - Mode: by Jenkinsfile
   - Script Path: `accounts-service/Jenkinsfile`
5. Scan Multibranch Pipeline Triggers: Every 5 minutes

**Для всего приложения (зонтичный):**

1. Jenkins → New Item → Multibranch Pipeline
2. Name: `bank-system-umbrella`
3. Branch Sources: Git
   - Repository URL: `https://github.com/YOUR_USERNAME/bankapp.git`
4. Build Configuration:
   - Script Path: `Jenkinsfile`

### Доступные Jenkinsfiles

```
accounts-service/Jenkinsfile       # Accounts Service CI/CD
cash-service/Jenkinsfile           # Cash Service CI/CD
transfer-service/Jenkinsfile       # Transfer Service CI/CD
exchange-service/Jenkinsfile       # Exchange Service CI/CD
exchange-generator/Jenkinsfile     # Exchange Generator CI/CD
notification-service/Jenkinsfile   # Notification Service CI/CD
front-ui/Jenkinsfile              # Front UI CI/CD
Jenkinsfile                       # Umbrella pipeline (все сервисы)
```

### Workflow

**Feature Development:**
```
1. Create branch: git checkout -b feature/new-feature
2. Make changes and commit
3. Push: git push origin feature/new-feature
4. Jenkins builds and deploys to 'test' namespace automatically
5. Run tests in test environment
6. Merge to main
7. Jenkins requires manual approval for prod deployment
```

### Ручной запуск deployment

```bash
# Для конкретного сервиса
# Откройте Jenkins -> accounts-service -> main -> Build Now

# Для всего приложения
# Откройте Jenkins -> bank-system-umbrella -> main -> Build Now
```

## 🌐 Доступ к приложению

### После развертывания

**Через Port-Forward (локальный доступ):**

```bash
# Front UI
kubectl port-forward -n <namespace> svc/bank-system-front-ui 8088:8088
# Затем откройте: http://localhost:8088

# OAuth Server (Keycloak)
kubectl port-forward -n <namespace> svc/bank-system-oauth-server 8080:8080
# Затем откройте: http://localhost:8080
```

**Через Ingress (если настроен):**

Добавьте в `/etc/hosts` (Linux/Mac) или `C:\Windows\System32\drivers\etc\hosts` (Windows):

```
127.0.0.1 bank.dev.local
127.0.0.1 bank.test.local
127.0.0.1 bank.prod.local
```

Затем откройте:
- DEV: http://bank.dev.local
- TEST: http://bank.test.local
- PROD: http://bank.prod.local

### Keycloak Admin Console

```bash
kubectl port-forward -n <namespace> svc/bank-system-oauth-server 8080:8080
```

- URL: http://localhost:8080
- Username: `admin`
- Password: `admin`

## 💻 Локальная разработка

### Сборка проектов

```bash
# Сборка всех сервисов
mvn clean package -DskipTests

# Сборка отдельного сервиса
cd accounts-service
mvn clean package
```

### Запуск локально (требует PostgreSQL и Keycloak)

```bash
# Accounts Service
cd accounts-service
mvn spring-boot:run

# Cash Service
cd cash-service
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082
```

### Сборка Docker образов

```bash
# Все образы
cd bankapp
.\build_images.bat  # Windows
./build_images.sh   # Linux/Mac

# Отдельный образ
docker build -t bank-app/accounts-service:v1 ./accounts-service
```

## ✨ Функциональность

### Для пользователей:
- ✅ Регистрация нового аккаунта
- ✅ Аутентификация через OAuth 2.0 (Keycloak)
- ✅ Управление счетами в разных валютах
- ✅ Просмотр баланса
- ✅ Пополнение счета
- ✅ Снятие средств
- ✅ Переводы между своими счетами
- ✅ Переводы другим пользователям
- ✅ Изменение профиля
- ✅ Смена пароля

### Технические возможности:
- ✅ Kubernetes-native Service Discovery (DNS)
- ✅ Ingress/Gateway API для маршрутизации
- ✅ ConfigMaps и Secrets для конфигурации
- ✅ OAuth2 Resource Server для межсервисной авторизации
- ✅ Health checks для всех сервисов
- ✅ Helm tests для валидации развертывания
- ✅ Multi-environment deployment (dev/test/prod)
- ✅ CI/CD с Jenkins
- ✅ High Availability в production

## 📁 Структура проекта

```
bankapp/
├── accounts-service/         # Сервис аккаунтов
│   ├── src/
│   ├── Dockerfile
│   └── Jenkinsfile
├── cash-service/            # Сервис кэша
│   ├── src/
│   ├── Dockerfile
│   └── Jenkinsfile
├── transfer-service/        # Сервис переводов
├── exchange-service/        # Сервис обмена валют
├── exchange-generator/      # Генератор курсов
├── notification-service/    # Сервис уведомлений
├── front-ui/               # Веб-интерфейс
├── bank-umbrella/          # Зонтичный Helm chart
│   ├── charts/
│   │   ├── postgres-db/
│   │   ├── oauth-server/
│   │   ├── accounts-service/
│   │   ├── cash-service/
│   │   ├── transfer-service/
│   │   ├── exchange-service/
│   │   ├── exchange-generator/
│   │   ├── notification-service/
│   │   ├── front-ui/
│   │   └── ingress/
│   ├── values.yaml          # Базовые значения
│   ├── values-dev.yaml      # DEV окружение
│   ├── values-test.yaml     # TEST окружение
│   ├── values-prod.yaml     # PROD окружение
│   ├── deploy-dev.bat/.sh   # Скрипты развертывания
│   ├── deploy-test.bat/.sh
│   └── deploy-prod.bat/.sh
├── Jenkinsfile             # Зонтичный pipeline
├── build_images.bat        # Сборка Docker образов
└── README.md
```

## 🔧 Troubleshooting

### Поды не запускаются

```bash
# Проверка статуса пода
kubectl describe pod <pod-name> -n <namespace>

# Просмотр логов
kubectl logs <pod-name> -n <namespace>

# События в namespace
kubectl get events -n <namespace> --sort-by='.lastTimestamp'
```

### ImagePullBackOff

```bash
# Проверить, что образы собраны
docker images | grep bank-app

# Пересобрать образы
.\build_images.bat  # Windows
```

### CrashLoopBackOff

```bash
# Проверить логи приложения
kubectl logs <pod-name> -n <namespace> --previous

# Проверить ConfigMaps и Secrets
kubectl get configmap -n <namespace>
kubectl get secret -n <namespace>
```

### Проблемы с базой данных

```bash
# Подключение к PostgreSQL
kubectl exec -it bank-system-postgres-db-0 -n <namespace> -- psql -U postgres

# Проверка схем
\l  # список баз данных
\dn  # список схем
```

### Helm deployment fails

```bash
# Проверить синтаксис
helm lint bank-umbrella

# Dry-run для проверки манифестов
helm install bank-system bank-umbrella --dry-run --debug

# Обновить зависимости
helm dependency update bank-umbrella
```

## 📚 Дополнительная документация

- [Jenkins Setup Guide](jenkins_setup.md) - Настройка CI/CD
- [Multi-Environment Deployment](multi_env_deployment.md) - Деплой в разные окружения
- [Sprint 10 Status](sprint10_status.md) - Статус выполнения задач

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- **Your Name** - Initial work

## 🙏 Acknowledgments

- Spring Boot Team
- Kubernetes Community
- Helm Contributors