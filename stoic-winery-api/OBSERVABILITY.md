## 🛠️ Observability

Real-time monitoring of metrics, logs, and distributed traces. The observability layer is production-grade and fully
integrated with Docker deployment.

| Component               | Purpose                                                                                                 |
|-------------------------|---------------------------------------------------------------------------------------------------------|
| Spring Boot Actuator    | Application state endpoints                                                                             |
| Micrometer + Prometheus | Metrics collection (CPU, memory, HTTP requests, HikariCP, JDBC via `datasource-micrometer-spring-boot`) |
| Grafana                 | Dashboards & visualization (external port 3030 → internal 3000)                                         |
| Loki                    | Logs aggregation from Spring Boot, trace-aware                                                          |
| Tempo                   | Distributed tracing (Zipkin-compatible on internal port 9411 for span ingestion)                        |
| Loki Logback Appender   | Sends logs to Loki; supports correlation via `%mdc{traceId}`                                            |

### 🔗 Grafana Access

- URL: [http://localhost:3030](http://localhost:3030)

    - Anonymous access enabled with admin privileges.

### Metrics

- Metrics path: `/api/actuator/prometheus`

    - Collected automatically by Micrometer:

        - JVM metrics: memory, GC, threads

        - HTTP server requests

        - HikariCP connection pool

        - JDBC queries (via `datasource-micrometer-spring-boot`)

        - Tomcat sessions

        - System and process metrics

    - Custom domain metrics:

        - `wine.total.count`, `wine.average.rating`

        - Service-specific metrics: `wine.service`, `review.service`, `order.service`, `auth.service`

    - Tagging strategy:

        - Standard: `method`, `class`, `uri`, `status`, `outcome`, `application`

        - Domain-specific: e.g., `wine.id`

    - Example endpoints:

        - `/api/actuator/metrics`

        - `/api/actuator/metrics/{metricName}?tag=...`

### Grafana Dashboards

Predefined Grafana dashboards are loaded from `docker/grafana/dashboards` when the Docker stack starts.

Key dashboards:

- **API Overview**
    - **Purpose**: high-level health and performance of the backend.
    - **Shows**:
        - RPS and latency (`http_server_requests_seconds_*`) by `uri`, `status`, `method`.
        - Error ratio (4xx/5xx).
        - Slowest endpoints by response time (p95/p99).
    - **Key labels**: `app="wine-store-api"`, `uri`, `status`, `method`, `outcome`.

    - **Database & Persistence**
        - **Purpose**: JDBC / HikariCP and SQL performance.
        - **Shows**:
            - Connection pool utilization (HikariCP).
            - SQL execution time (metrics from `datasource-micrometer-spring-boot`).
            - Database error rate.
        - **Key labels**: `pool`, `exception`, `outcome`.

    - **Errors & Traces**
        - **Purpose**: fast investigation of errors and slow requests.
        - **Shows**:
            - 5xx count by `uri` / `status`.
            - Links from panels to Tempo traces (via `traceId`).
            - Slowest requests (p95/p99 latency).

Grafana is available at `http://localhost:3030` (anonymous admin access in local Docker setup).

### Prometheus Alerting Rules

Alerting rules are defined in `docker/prometheus/rules.yml` and are loaded automatically by Prometheus when the Docker
stack starts.

Main alert groups:

- **High HTTP Error Rate**
    - **Idea**: notify when the share of 5xx responses grows.
    - **Typical logic (in plain language)**:
        - If over the last 5 minutes the share of 5xx errors for a given endpoint (based on
          `http_server_requests_seconds_count` with `status^~"5.."`) exceeds a threshold (for example 5%), the alert
          fires.
    - **Impact**: users frequently see errors; the service is unstable.

    - **High Request Latency**
        - **Idea**: detect performance degradation.
        - **Typical logic**:
            - If p95 latency for HTTP requests is higher than a threshold (for example 1s) for 5 minutes for a specific
              `uri`, the alert fires.
        - **Impact**: pages or operations become noticeably slow; UX degrades.

    - **Database Saturation**
        - **Idea**: monitor the connection pool and database as a bottleneck.
        - **Typical logic**:
            - If the connection pool is close to maximum usage for an extended period or SQL execution time
              significantly increases, the alert fires.
        - **Impact**: the database becomes a bottleneck; timeouts and errors are likely.

Alerts are based on metrics with label `app="wine-store-api"` and are visible in the **Alerts** page of the Prometheus
UI (`http://localhost:9090`). They can be reused when integrating with Alertmanager for notifications.

### Logs

- Logs are emitted by Spring Boot using Logback and shipped to Loki.

    - Log entries include `traceId` and `spanId` for correlation with Tempo traces.

    - Runtime log level management via `/api/actuator/loggers`.

    - Example Loki queries:

        - `{app="wine-store-api"}`

        - `{level="ERROR"}`

    - JSON or MDC-enhanced format required for correct Trace+Log correlation in Grafana.

### Distributed Tracing

- Instrumentation: Micrometer Tracing via OpenTelemetry Bridge (`micrometer-tracing-bridge-otel`)

- Export: Zipkin-compatible format to Tempo (internal Docker port 9411 for spans)

- Automatic tracing for:

    - HTTP requests

    - Controller → Service boundaries

    - Security filter chain

    - Database interactions

- Trace context propagates through logs and metrics.

- Enables latency breakdown, bottleneck detection, and correlation of slow requests with logs and metrics.

- Note: Tempo port 9411 is internal for span ingestion; request visualization is performed via Grafana dashboards.

### Tracing Naming Convention

The service uses the unified tracing naming format: `<domain>/<action>`

**Examples of trace names:**

| Domain   | Operation           | Trace Name                   |
|----------|---------------------|------------------------------|
| Wine     | Create              | `wine/create`                |
| Wine     | Find                | `wine/find`                  |
| Wine     | Find all            | `wine/find-all`              |
| Wine     | Delete              | `wine/delete`                |
| Wine     | Exists              | `wine/exists`                |
| Wine     | Update image        | `wine/update-image`          |
| Wine     | Update rating       | `wine/update-rating`         |
| Review   | Create              | `review/create`              |
| Review   | Find by wine        | `review/find/by-wine`        |
| Order    | Create              | `order/create`               |
| Order    | Find all            | `order/find-all`             |
| Order    | Delete              | `order/delete`               |
| Order    | Set paid            | `order/set-paid`             |
| Order    | Find by ID          | `order/find/by-id`           |
| Order    | Find by number      | `order/find/by-number`       |
| Order    | Find by user        | `order/find/by-user`         |
| User     | Get or create       | `user/get-or-create`         |
| User     | Register            | `user/register`              |
| User     | Sync data           | `user/sync-data`             |
| User     | Update role         | `user/update-role`           |
| User     | Update Telegram ID  | `user/update-tg-id`          |
| User     | Find by ID          | `user/find/by-id`            |
| User     | Find by email       | `user/find/by-email`         |
| User     | Find by Telegram ID | `user/find/by-tg-id`         |
| User     | Find by role        | `user/find/by-role`          |
| Auth     | Authenticate        | `auth/authenticate`          |
| Auth     | Login               | `auth/login`                 |
| Auth     | Logout              | `auth/logout`                |
| Auth     | Register            | `auth/register`              |
| Auth     | Validate token      | `auth/validate-token`        |
| Auth     | Load user           | `auth/load-user`             |
| Telegram | Start               | `telegram/start`             |
| Telegram | Receive update      | `telegram/receive-update`    |
| Telegram | Send notification   | `telegram/send-notification` |
| Telegram | Send picture        | `telegram/send-picture`      |
| Telegram | Register by order   | `telegram/register-by-order` |
| Image    | Delete              | `image/delete`               |
| Image    | Update              | `image/update`               |

**Database spans:**

Database operations use the format: `<datasource>:<operation>`

Examples:

- `wine_store_db:query` — general query
- `jdbc:select/users/by-email` — specific select operation
- `jdbc:insert/orders` — insert operation

### Actuator Endpoints

- Base path: `/api/actuator`

    - Key endpoints:

        - `/api/actuator/health` — application health status

        - `/api/actuator/info` — build and application metadata

        - `/api/actuator/metrics` — available metrics registry

        - `/api/actuator/metrics/{name}` — detailed metric inspection

        - `/api/actuator/prometheus` — Prometheus scrape endpoint

        - `/api/actuator/loggers` — runtime log configuration

This setup ensures full observability across the application stack, both for local development and Docker-based
deployment, with clear correlation between metrics, logs, and traces.