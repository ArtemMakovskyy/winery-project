# Winery Admin UI

## 📌 Description

**Winery Admin UI** is a UI application for administering a wine store. The application is implemented as a **thin
client**, which does not contain business logic for data persistence and interacts with backend services exclusively via
HTTP API.

# 👥 User Roles and Available Capabilities

## 📋 Access Matrix

| Functionality               | Manager | Administrator |
|-----------------------------|---------|---------------|
| Create wine                 | ✅       | ❌             |
| Delete wine                 | ✅       | ❌             |
| Upload / update wine images | ✅       | ❌             |
| View orders                 | ✅       | ❌             |
| Mark order as _paid_        | ✅       | ❌             |
| Delete orders               | ✅       | ❌             |
| Change user roles           | ❌       | ✅             |

## 🧑‍💼 Manager

The **Manager** is responsible for business operations of the wine store:

- Managing wines (create, delete, images)

- Managing orders (view, mark as paid, delete)

**Login credentials (development environment):**

- **Email:** `manager12345@gmail.com`

- **Password:** `12345`

## 👑 Administrator

The **Administrator** performs only system-level functions:

- Managing user roles

**Login credentials (development environment):**

- **Email:** `admin12345@gmail.com`

- **Password:** `12345`

## 🧱 Architecture

### General Approach

The project is built on:

- **Spring MVC** — for the UI layer

- **Thymeleaf** — for server-side HTML rendering

- **Feign Client** — for communication with backend services

- **Package-by-feature** — for code organization

- **Backend-for-Frontend (BFF) pattern** — UI acts as a separate client to the backend API

### Architecture Diagram

```
UI (Spring MVC + Thymeleaf) → Controller
        ↓
Service (orchestration only)
        ↓
Feign Client
        ↓
Backend (wine-store-service)
```

### Key Principles

- UI **does not work directly with the database**

- UI **does not contain domain business logic**

- All business logic, validation, and data persistence are handled in the backend service

- UI is responsible only for:

    - handling HTTP requests

    - calling backend APIs

    - preparing data for rendering

## 📦 Project Structure

The project follows the **package-by-feature** principle, where each feature contains all required components:

```
features/
 └─ wine/
     ├─ WineController      # MVC Controller (UI layer)
     ├─ WineService         # Orchestration / coordination logic
     ├─ WineFeignClient     # HTTP client to backend API
     ├─ dto/                # DTO / ViewModel
     └─ view/               # Thymeleaf templates
```

## 🎯 Role of the UI Application

The UI application acts as a **Backend-for-Frontend**:

- adapts backend APIs to UI needs

- aggregates data from one or multiple backend calls

- isolates the frontend from changes in backend contracts

## 🛠 Technologies & Versions

### Platform

- **Java:** 21

- **Spring Boot:** 3.2.1

- **Spring Cloud:** 2023.0.3

- **Maven**

### UI & Web

- **Spring MVC** (spring-boot-starter-web)

- **Thymeleaf** (server-side rendering)

- **Bean Validation** (spring-boot-starter-validation)

- **Spring AOP** (cross-cutting concerns)

### Backend Integration

- **Spring Cloud OpenFeign** — HTTP client for backend APIs

- **Feign Form** — multipart/form-data support

- **Feign OkHttp** — HTTP client implementation

### Observability

[OBSERVABILITY.md](OBSERVABILITY.md)

- **Spring Boot Actuator** — health, metrics, endpoints

- **Micrometer** — metrics & tracing abstraction

- **Prometheus registry** — metrics export

- **OpenTelemetry** — distributed tracing

- **Zipkin exporter** — trace visualization

- **Grafana Loki (logback appender)** — centralized logging

- **Tracing naming convention:** `<domain>/<action>` format (e.g., `wine/create`, `order/find-all`, `ui/wine-form`)

### Security & Utilities

- **JWT (java-jwt)** — access token handling

- **Lombok** — boilerplate reduction

### Testing

- **spring-boot-starter-test**

> ❗ The UI application **does not use JPA, Hibernate, or direct database access**. All data interactions happen via
> backend APIs.

## 🚀 Running the Project

> ⚠️ For proper functionality, the backend service `wine-store-service` and database must be running.

### Local Run

To run the UI (admin panel) locally:

```
cd winery-admin-ui
mvn clean spring-boot:run
```

UI will be available at:

```
http://localhost:8081/login
```

To run the backend (API) locally:

```
cd stoic-winery-api
mvn clean spring-boot:run
```

MySQL database must be available on the port specified in `.env`.

### Docker Run

The entire stack (backend, database, UI, frontend, Prometheus, Grafana, Loki, Tempo) can be launched via **Docker
Compose**:

```
docker-compose up --build
```

- **Backend (API):** `http://localhost:8080/api`

- **Admin UI:** `http://localhost:8081/login`

- **Frontend (wine-site):** `http://localhost:3000`

- **Prometheus:** `http://localhost:9090`

- **Grafana:** `http://localhost:3030`

- **Loki:** `http://localhost:3100`

- **Tempo / Zipkin:** `http://localhost:9411`