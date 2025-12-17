# secure-api (Lab1)

Учебный REST API на Spring Boot с JWT-аутентификацией и демонстрацией базовых мер защиты (SQLi/XSS).

## Запуск

Требования: Java 17, Maven.

1) (Рекомендуется) задать секрет для подписи JWT:

```bash
# Windows PowerShell
$env:JWT_SECRET = "very-long-random-secret-at-least-32-bytes"
```

2) Запустить приложение:

```bash
mvn spring-boot:run
```

По умолчанию используется H2 in-memory (`jdbc:h2:mem:demo`).

## API

Базовый URL: `http://localhost:8080`

### 1) Авторизация

#### POST `/auth/login`

Логин по email+паролю. Для удобства лабораторной работы при старте создаётся демо-пользователь:

- email: `user@example.com`
- password: `password`

Запрос:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"user@example.com\",\"password\":\"password\"}"
```

Успех (200 OK):

```json
{ "token": "<JWT>" }
```

Ошибка (401 Unauthorized): пустое тело.

### 2) Публичный эндпоинт

#### GET `/api/public`

Доступен без токена.

```bash
curl http://localhost:8080/api/public
```

Пример ответа:

```json
{ "message": "public OK" }
```

### 3) Приватные данные

#### GET `/api/data`

Требует JWT в заголовке `Authorization: Bearer <token>`.

1) Получить токен через `/auth/login`.
2) Вызвать эндпоинт:

```bash
curl http://localhost:8080/api/data \
  -H "Authorization: Bearer <JWT>"
```

Пример ответа:

```json
[
  {"id":1,"author":"Alice &lt;script&gt;bad()&lt;/script&gt;","text":"Hello"},
  {"id":2,"author":"Bob","text":"Private for: user@example.com"}
]
```

## Реализованные меры защиты

Ниже — что именно сделано в коде и за счёт чего это работает.

### 1) Защита от SQL Injection (SQLi)

- Используется Spring Data JPA (`JpaRepository`) и типобезопасные методы репозитория (например, `findByEmail(String email)`).
- В проекте отсутствуют конкатенации строк для построения SQL-запросов и отсутствуют `Statement`/`createStatement()`.
- JPA/ORM формирует запросы с параметризацией (аналог prepared statements), поэтому пользовательский ввод не «встраивается» в SQL как часть синтаксиса.

Дополнительно:
- Для входных данных на логине включена валидация `@Valid` + ограничения `@NotBlank`, `@Email`, что уменьшает поверхность атаки и не пропускает явно некорректные значения.

### 2) Защита от XSS

- Потенциально опасные строки экранируются перед выдачей в ответ:
  - используется `org.apache.commons.text.StringEscapeUtils.escapeHtml4(...)`.
- Благодаря HTML-экранированию символы `<`, `>`, `"`, `'` и т.п. преобразуются в HTML-сущности (например, `<script>` → `&lt;script&gt;`).
- Это предотвращает исполнение внедрённого HTML/JS в браузере, если клиентская часть бездумно вставит поле в DOM как HTML.

Важно: API возвращает JSON, но XSS актуален на стороне фронтенда. Здесь защита показана именно как серверная «санитизация на выходе» для демонстрации.

### 3) Аутентификация и доступ к защищённым ресурсам

#### JWT (Bearer Token)

- Логин выдаёт JWT токен с subject = email пользователя.
- Подпись: HS256.
- Секрет берётся из конфигурации `security.jwt.secret` (поддерживается ENV `JWT_SECRET`).
- Время жизни токена: `security.jwt.expiration-ms` (по умолчанию 1 час).

#### Spring Security (stateless)

- Сессии отключены: `SessionCreationPolicy.STATELESS`.
- CSRF выключен, потому что нет cookie-сессий и сервер не хранит состояние; авторизация идёт только через `Authorization: Bearer ...`.
- Разрешено без авторизации: `/auth/**`, `/api/public`.
- Любые другие запросы требуют валидный JWT.

#### Проверка токена

- Фильтр читает заголовок `Authorization`.
- Если токен корректен и подпись/срок валидны — в `SecurityContext` выставляется `Authentication`.
- Иначе запрос продолжает обработку как неаутентифицированный и будет заблокирован правилами доступа.

### 4) Хранение пароля

- Пароли не хранятся в открытом виде.
- Для хеширования используется BCrypt (`BCryptPasswordEncoder(12)`).
- При логине выполняется сравнение через `matches(raw, hash)`.
