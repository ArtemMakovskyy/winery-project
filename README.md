# 🍷 "Stoic winery" 
Welcome to the **Stoic Winery** project — a full-stack wine store application with **frontend**, backend, admin panel, and observability features.

## ▶️ How to set up and start the project

### Soft requirements

- Docker

- Postman

- Git

- Browser

---
### Installation

Clone the repository from GitHub:

`git clone https://github.com/ArtemMakovskyy/winery-project.git`

Start Docker.

If necessary, configure the database and Telegram Bot parameters in the `.env` file.
By default, the Telegram bot is disabled and all required required parameters are already set.
No additional configuration is required.

Navigate to the root directory of the project and run:

**First start:**

`docker-compose up`

**Next starts:**

`docker-compose up --no-build`

---

### 🐳 Docker Compose Profiles

The project provides three Docker Compose configurations for different use cases:

| File | Purpose | Services Included |
|------|---------|-------------------|
| `docker-compose.yaml` | **Default** — Basic application stack | Backend, Frontend, Admin UI, MySQL, Prometheus, Loki, Tempo, Grafana |
| `docker-compose.dev.yaml` | **Full Observability** — Development with complete monitoring | All default services + **exposed ports for Loki, Tempo, Zipkin** + **Debug port for backend** |
| `docker-compose.inv.yaml` | **Infrastructure Only** — Spring apps environment | MySQL, Prometheus, Loki, Tempo, Grafana (no application containers) |

**Usage:**

```bash
# Default stack (basic observability)
docker-compose up

# Full observability stack (recommended for development)
docker-compose -f docker-compose.dev.yaml up

# Infrastructure only (for running Spring apps locally)
docker-compose -f docker-compose.inv.yaml up
```

> **⚠️ Default Configuration Limitations:**
> 
> When using `docker-compose.yaml` (default), the following observability features are **limited**:
> - **Loki** — no exposed port (logs aggregation works internally, but external access unavailable)
> - **Tempo** — no exposed port (tracing works internally, but Zipkin UI unavailable)
> - **Backend Debug Port** — not exposed (JDWP debugging unavailable)
> 
> For **full observability access** (Grafana, Prometheus, Loki, Tempo/Zipkin UI, backend debugging), use:
> ```bash
> docker-compose -f docker-compose.dev.yaml up
> ```

---

### 🌐 Deployment Configuration

> **Important for Frontend Deployment:**
> 
> If you are deploying the application to a server, you must update the API base URL in the frontend configuration.
> 
> **File:** `wine_site_project/src/utils/fetchClient.ts`
> 
> **Change:**
> ```typescript
> export const BASE_URL = 'http://localhost:8080/api';
> ```
>
> **To your server's API URL (format: `http://IP-address:port/api`):**
> ```typescript
> // Example (replace this 204.204.204.33 with your actual server IP and port)
> export const BASE_URL = 'http://204.204.204.33:8080/api';
> ```
> This ensures the frontend can correctly communicate with the backend API after deployment.

## 🚀 How to use the application

### 1️⃣ Wine Store Frontend (Customer UI)

`http://localhost:3000/#/products`

---

### 2️⃣ Wine Store Backend API

Swagger UI:

`http://localhost:8080/api/swagger-ui/index.html#/`

You can also use the API via **Postman**.

---

### 3️⃣ Winery Admin Panel (UI microservice)

The **Admin Panel** is a separate UI application for internal management tasks.

Admin Panel URL:

`http://localhost:8081`

It provides:

- wine management (create, delete, image upload)
    
- order management (view, mark as paid, delete)
    
- user role management (administrator only)
    

**Login as ADMINISTRATOR**

- **Email:** `admin12345@gmail.com`
    
- **Password:** `12345`
- 
**Login as MANAGER

- **Email:** `manager12345@gmail.com`
    
- **Password:** `12345`

---
## 🧩 Project structure overview

- **wine-site-frontend** — customer-facing React application
    
- **stoic-winery-api** — main backend REST API
    
- **winery-admin-ui** — separate admin UI microservice
    
- **observability stack** — Prometheus, Grafana, Loki, Tempo
	- **Grafana:** `http://localhost:3030/dashboards`
	- **Prometheus:** `http://localhost:9090`
	- **Loki:** `http://localhost:3100`
	- **Tempo / Zipkin:** `http://localhost:9411`   
    
---
## 📚 Detailed project documentation

👓 **Stoic Winery site frontend**  
[https://github.com/ArtemMakovskyy/winery-project/blob/master/wine_site_project/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/master/wine_site_project/README.md)

👓 **Stoic Winery API backend**  
[https://github.com/ArtemMakovskyy/winery-project/blob/master/stoic-winery-api/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/master/stoic-winery-api/README.md)

👓 **Stoic Winery ADMIN PANEL**  
[https://github.com/ArtemMakovskyy/winery-project/blob/winery-admin-ui/winery-admin-ui/README.md](https://github.com/ArtemMakovskyy/winery-project/blob/winery-admin-ui/winery-admin-ui/README.md)

---

## 🛠 Tech Stack

- **Language:** Java 21
    
- **Framework:** Spring Boot 3.2.1
    
- **Architecture:** REST, Modular Monolith
    
- **Database:** MySQL, Liquibase migrations
    
- **Security:** Spring Security, JWT
    
- **Integration:** Telegram Bots
    
- **Observability:** Prometheus, Grafana, Loki, Tempo
    
- **Frontend:** React
    
- **Testing:** JUnit, Mockito, Testcontainers

---
👓 **Observability Documentation**

Centralized monitoring and tracing documentation for all services, covering metrics, logs, and distributed tracing tools.

- **Backend API (stoic-winery-api)**
    - [OBSERVABILITY.md](stoic-winery-api/OBSERVABILITY.md) — observability setup and usage

- **Admin Panel UI (winery-admin-ui)**
    - [OBSERVABILITY.md](winery-admin-ui/OBSERVABILITY.md) — observability setup and usage

These documents describe the metrics, tags, events, and spans used, along with instructions for integrating Prometheus, Grafana, Loki, and Tempo/Zipkin for each service.

### Tracing Naming Convention

Both services use the unified tracing naming format: `<domain>/<action>[/<detail>]`

Examples:
- Backend: `order/create`, `wine/find`, `user/register`, `telegram/send-notification`
- Admin UI: `wine/create`, `order/find-all`, `user/update-role`, `ui/wine-form`

This format provides:
- Consistent naming across services
- Clear trace visualization in Grafana/Tempo
- Easy correlation between logs, metrics, and traces