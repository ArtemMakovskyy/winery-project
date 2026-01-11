# 🍷 Stoic Winery API - Backend

This repository contains the **Spring Boot backend** for the Stoic Winery project.

Development repository with all commits: [https://github.com/ArtemMakovskyy/wine-store-app](https://github.com/ArtemMakovskyy/wine-store-app)

---

## 🏗️ Architecture Evolution

The project evolved through several patterns to improve maintainability and scalability:

- **Layered MVC:** Initial standard implementation.
- **Package by Feature:** Grouping code by domain features.
- **Modular Monolith (Current):** Multi-module Maven project for production.

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

| Component | Description |
|-----------|-------------|
| Java 21 | Programming language |
| Spring Boot 3.2.1 | Framework |
| Maven | Dependency management & build |
| REST API | Communication layer |
| JWT | Authentication & authorization |
| Spring Data JPA | Database ORM |
| MySQL & H2 | Databases |
| Liquibase | Database migrations |
| Lombok & MapStruct | DTO mapping & boilerplate reduction |
| Telegram Bots | Notifications |
| JUnit, Mockito, Testcontainers | Testing |
| Springdoc OpenAPI | API documentation |

---

## 🛠️ Observability

Real-time monitoring of metrics, logs, and traces:

| Component | Purpose |
|-----------|---------|
| Spring Boot Actuator | Application state endpoints |
| Micrometer + Prometheus | Metrics collection (CPU, memory, HTTP requests) |
| Grafana | Dashboards & visualization |
| Loki | Logs aggregation from Spring Boot |
| Tempo | Distributed tracing |
| Zipkin Brave | Trace reporting |
| Loki Logback Appender | Sends logs to Loki |

### 🔗 Grafana Access
- URL: [http://localhost:3030](http://localhost:3030)
- Anonymous access enabled with admin privileges.

### Metrics
- Metrics path: `/actuator/prometheus`
- Example metrics: `http_server_requests_seconds_count`, `jvm_memory_used_bytes`

### Logs
- Explore → Loki
- Example queries: `{app="wine-store-api"}`, `{level="ERROR"}`

### Traces
- Traces → Tempo
- Inspect request paths for end-to-end tracing.

---

## 🧭 API Endpoints Overview

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/auth/login` | POST | Login user, return JWT tokens | No |
| `/api/auth/register` | POST | Register user | No |
| `/api/auth/logout` | POST | Logout user, invalidate token | Yes |
| `/api/users/{id}/role` | PUT | Update user role | ADMIN |
| `/api/wines` | GET | List wines (pagination & sorting available) | All |
| `/api/wines` | POST | Add new wine | MANAGER |
| `/api/wines/{id}` | GET | Get wine by ID | All |
| `/api/wines/{id}` | DELETE | Delete wine | MANAGER |
| `/api/wines/{id}/image` | PATCH | Update wine images | MANAGER |
| `/api/reviews` | POST | Add review for a wine | All |
| `/api/reviews/wines/{wineId}` | GET | Get reviews for a wine | All |
| `/api/orders` | GET | List all orders | All |
| `/api/orders` | POST | Place an order | All |
| `/api/orders/{id}/paid` | PATCH | Mark order as PAID | MANAGER |
| `/api/orders/{id}` | GET | Get order by ID | All |
| `/api/orders/{id}` | DELETE | Delete order | MANAGER |
| `/api/orders/users/{userId}` | GET | Get orders by user ID | All |

---

## 🔒 Default Credentials

- **Admin:** `admin12345@gmail.com` / `12345`
- **Manager:** `manager12345@gmail.com` / `12345`

---

## 💬 Notes

- Users are identified by first name, last name, and phone number.
- Duplicate reviews or orders for the same user/wine are handled automatically.
- Telegram notifications for new orders and status updates are available.