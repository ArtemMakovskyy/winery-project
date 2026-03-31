# 🍷 Stoic Winery API - Backend

This repository contains the **Spring Boot backend** for the Stoic Winery project.

Development repository with all
commits: [https://github.com/ArtemMakovskyy/wine-store-app](https://github.com/ArtemMakovskyy/wine-store-app)

---

## 🏗️ Architecture Evolution

The project evolved through several patterns to improve maintainability and scalability:

- **Layered MVC:** Initial standard implementation.
- **Package by Feature:** Grouping code by domain features.
- **Modular Monolith (Current):** Multi-module Maven project for production.

---

## 📬 Domain Events Architecture

The application uses **Spring Domain Events** for loose coupling between services:

| Event                                                                                                 | Trigger                    | Listeners              |
|-------------------------------------------------------------------------------------------------------|----------------------------|------------------------|
| [`OrderEvent`](stoic-winery-api/common/src/main/java/com/winestoreapp/common/event/OrderEvent.java)   | Order created/paid/deleted | Telegram notifications |
| [`ReviewEvent`](stoic-winery-api/common/src/main/java/com/winestoreapp/common/event/ReviewEvent.java) | Review created/deleted     | Wine rating updates    |

**Event Flow Example (Order Notification):**

```
1. User creates order → OrderEvent published (AccessType.CREATE)
2. TelegramBotNotificationService listens (@Async, @TransactionalEventListener AFTER_COMMIT)
3. Notification sent to user's Telegram chat
```

**Event Flow Example (Review Rating Update):**

```
1. User creates review → ReviewEvent published (AccessType.CREATE)
2. WineServiceImpl listens (@Async, @EventListener)
3. Wine average rating updated in database
```

**Benefits:**

- Decoupled services (ReviewService ↔ WineService)
- Async processing via `@Async` listeners
- Transactional consistency (`@TransactionalEventListener`)
- Better scalability and maintainability

---

## 🚀 Features

- View all wines sorted by rating.
- Access detailed wine cards.
- Leave reviews and ratings (duplicate reviews are replaced automatically).
- Place orders and track them.
- Receive notifications via Telegram.
- User management (Admin can assign manager roles; managers can add/edit/delete wines).
- Access control for Admin, Manager, and Users.
- Logout functionality.
- API Documentation: Once the API is running, you can use the controller's detailed documentation.
    - Link to launch: http://localhost:8080/api/swagger-ui/index.html
    - Default Admin: login - admin12345@gmail.com, password - 12345
    - Default Manager: login - manager12345@gmail.com, password - 12345

---

## 🖥️ Technologies & Tools

| Component                      | Description                         |
|--------------------------------|-------------------------------------|
| Java 21                        | Programming language                |
| Spring Boot 3.2.1              | Framework                           |
| Maven                          | Dependency management & build       |
| REST API                       | Communication layer                 |
| JWT                            | Authentication & authorization      |
| Spring Data JPA                | Database ORM                        |
| MySQL                          | Databases                           |
| Liquibase                      | Database migrations                 |
| Lombok & MapStruct             | DTO mapping & boilerplate reduction |
| Telegram Bots                  | Notifications                       |
| Redis                          | Cache & Rate Limiting               |
| Spring Domain Events           | Loose coupling between services     |
| JUnit, Mockito, Testcontainers | Testing                             |
| Springdoc OpenAPI              | API documentation                   |

---

## 🛠️ Observability

[OBSERVABILITY.md](OBSERVABILITY.md)

Real-time monitoring of metrics, logs, and traces:

| Component                  | Purpose                                                     |
|----------------------------|-------------------------------------------------------------|
| Spring Boot Actuator       | Application state endpoints                                 |
| Micrometer + Prometheus    | Metrics collection (CPU, memory, HTTP requests, DB metrics) |
| Grafana                    | Dashboards & visualization                                  |
| Loki                       | Logs aggregation from Spring Boot                           |
| Tempo                      | Distributed tracing backend                                 |
| OpenTelemetry (Micrometer) | Tracing instrumentation & context propagation               |
| Zipkin exporter (OTel)     | Trace export format (Zipkin-compatible, for Tempo)          |
| Loki Logback Appender      | Sends logs to Loki                                          |

### 🔗 Grafana Access

- URL: [http://localhost:3030](http://localhost:3030)

- Anonymous access enabled with admin privileges.

### Metrics

- Actuator base path: `/api/actuator`

- Prometheus scrape endpoint: `/api/actuator/prometheus`

- Example metrics: `http_server_requests_seconds_count`, `jvm_memory_used_bytes`

Metrics include:

- JVM, HTTP, system metrics

- **Database metrics via `datasource-micrometer-spring-boot`**
  (SQL execution time, pool usage, JDBC statistics)

### Logs

- Explore → Loki

- Example queries: `{app="wine-store-api"}`, `{level="ERROR"}`

- Logs are shipped via `loki-logback-appender`

- Loki endpoint configured via `LOKI_URL` environment variable

### Traces

- Traces → Tempo

- Instrumentation: **Micrometer Tracing + OpenTelemetry bridge**

- Export: **Zipkin-compatible format via OpenTelemetry exporter**

- Backend: **Tempo**

- Internal Docker endpoint for trace ingestion:

`http://tempo:9411/api/v2/spans`

- End-to-end tracing includes: HTTP requests, Controllers → services, Security filter chain, Database interactions

- Full correlation with logs and metrics via `traceId` / `spanId`

- **Tracing naming convention:** `<domain>/<action>[/<detail>]` format (e.g., `order/create`, `wine/find`,
  `user/register`)

---

## 🔴 Redis Integration

[REDIS.md](REDIS.md)

Redis is used for:

- **Caching** — wines (`@Cacheable`, `@CacheEvict`)
- **Rate Limiting** — request throttling for critical endpoints
- **JWT Blacklist** — revoked token storage

**Debug endpoints:** `/api/redis-debug/*`

---

## 🧭 API Endpoints Overview

| Endpoint                      | Method | Description                                 | Auth    |
|-------------------------------|--------|---------------------------------------------|---------|
| `/api/auth/login`             | POST   | Login user, return JWT tokens               | No      |
| `/api/auth/register`          | POST   | Register user                               | No      |
| `/api/auth/logout`            | POST   | Logout user, invalidate token               | Yes     |
| `/api/users/{id}/role`        | PUT    | Update user role                            | ADMIN   |
| `/api/wines`                  | GET    | List wines (pagination & sorting available) | All     |
| `/api/wines`                  | POST   | Add new wine                                | MANAGER |
| `/api/wines/{id}`             | GET    | Get wine by ID                              | All     |
| `/api/wines/{id}`             | DELETE | Delete wine                                 | MANAGER |
| `/api/wines/{id}/image`       | PATCH  | Update wine images                          | MANAGER |
| `/api/reviews`                | POST   | Add review for a wine                       | All     |
| `/api/reviews/wines/{wineId}` | GET    | Get reviews for a wine                      | All     |
| `/api/orders`                 | GET    | List all orders                             | All     |
| `/api/orders`                 | POST   | Place an order                              | All     |
| `/api/orders/{id}/paid`       | PATCH  | Mark order as PAID                          | MANAGER |
| `/api/orders/{id}`            | GET    | Get order by ID                             | All     |
| `/api/orders/{id}`            | DELETE | Delete order                                | MANAGER |
| `/api/orders/users/{userId}`  | GET    | Get orders by user ID                       | All     |

---

## 🔒 Default Credentials

- **Admin:** `admin12345@gmail.com` / `12345`
- **Manager:** `manager12345@gmail.com` / `12345`

---

## Performance Testing

See detailed load test results in **[PERFORMANCE_TESTS.md](PERFORMANCE_TESTS.md)**.

**Key Results:**

| Metric                | Value        |
|-----------------------|--------------|
| Average Response Time | 11-15ms      |
| Throughput            | 865+ req/sec |
| Max Response Time     | 31ms         |
| Error Rate            | < 1%         |

**Optimization Impact** (after adding 25 database indexes):

- Average response time: 27% faster (15ms -> 11ms)
- Max response time: 88% faster (255ms -> 31ms)

---

## Notes

- Users are identified by first name, last name, and phone number.
- Duplicate reviews or orders for the same user/wine are handled automatically.
- Telegram notifications for new orders and status updates are available.