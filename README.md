# Bank Card Management API

REST API для управления банковскими картами с авторизацией по JWT и разделением ролей (USER / ADMIN).

---

## Технологии

* Java 17
* Spring Boot 3 (Security, Data JPA, Validation)
* PostgreSQL
* Liquibase
* JWT
* Docker + Docker Compose
* Swagger UI

---

## Структура ролей

### Пользователь (USER):

* Просмотр своих карт (с фильтрацией и пагинацией)
* Переводы между своими картами
* Блокировка своей карты
* Пополнение карты по номеру

### Администратор (ADMIN):

* Просмотр всех карт
* Создание/удаление карт
* Активация/блокировка любой карты
* Получение информации о пользователях

---

## Запуск проекта локально

### 1. Клонируй репозиторий

```bash
git clone https://github.com/buhtakendl/bank_app
cd bank_app
```

### 2. Создай `.env` файл в корне

```dotenv
DB_HOST=postgres
DB_PORT=5433
DB_NAME=bank
DB_USER=bank_user
DB_PASSWORD=secret
JWT_SECRET=wLJd8aRZkNQh3VtPxG9uT7jVfY6c3DzBmLr2s4FvXzQ=
ENCRYPTION_SECRET=super-aes-key
SPRING_PROFILES_ACTIVE=dev
```

### 3. Собери jar-файл

```bash
./mvnw clean package
```

### 4. Запусти через Docker Compose

```bash
docker-compose up --build
```

Приложение будет доступно по адресу: `http://localhost:8080`

---

## Документация API

Swagger UI: [http://localhost:8080/swagger-ui/index.html]

---

## Возможности API

* `/api/auth/register`, `/login`
* `/api/users/me`, `/users/{id}`
* `/api/cards` 
* `/api/transfer` 
* `/api/admin/cards` 

---

## Профили Spring

* `dev` (локальная разработка)

Переключение через переменную окружения:

```bash
SPRING_PROFILES_ACTIVE=dev
```

---

## ToDo / Улучшения

* [ ] Rate Limiting
* [ ] Email notifications
