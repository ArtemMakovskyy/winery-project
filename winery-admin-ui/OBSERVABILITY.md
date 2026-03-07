## 🛠️ Observability

Real-time monitoring of metrics, logs, and distributed traces for the Admin UI (BFF layer).  
The UI is fully integrated into the shared Docker observability stack (Prometheus, Grafana, Loki, Tempo).

| Component                        | Purpose                                |
|----------------------------------|----------------------------------------|
| Spring Boot Actuator             | Application state endpoints            |
| Micrometer + Prometheus          | JVM and HTTP metrics export            |
| Grafana                          | Dashboards & visualization             |
| Loki                             | Centralized log aggregation            |
| Tempo                            | Distributed tracing backend            |
| Micrometer Tracing (OTel bridge) | Trace instrumentation                  |
| Zipkin exporter                  | Trace export format (Tempo-compatible) |
| Loki Logback Appender            | Log shipping with trace correlation    |

---

### 🔗 Grafana Access

- URL: `http://localhost:3030`

- Anonymous access enabled (admin role in local Docker setup)

---

## Metrics

Metrics are exposed via Actuator.

Default base path (if not overridden):  
`/actuator`

Prometheus scrape endpoint:  
`/actuator/prometheus`

Collected automatically:

- JVM metrics (memory, GC, threads)

- HTTP server metrics

- Feign client HTTP metrics

- System & process metrics

- Tomcat metrics

Typical metric examples:

- `http_server_requests_seconds`

- `jvm_memory_used_bytes`

- `process_cpu_usage`

Tagging strategy:

- Standard: `method`, `uri`, `status`, `outcome`, `exception`

- Domain-specific tags defined in:

    - `ObservationTags`

    - `ObservationNames`

    - `ObservationContextualNames`

Example Actuator endpoints:

- `/actuator/health`

- `/actuator/info`

- `/actuator/metrics`

- `/actuator/metrics/{name}`

- `/actuator/prometheus`

- `/actuator/loggers`

---

## Logs

Logging is handled by Logback and shipped to Loki via `loki-logback-appender`.

Log entries include:

- `traceId`

- `spanId`

This enables full correlation between:

- Logs

- Traces (Tempo)

- Metrics (Prometheus)

Example Loki queries:

`{app="winery-admin-ui"} {level="ERROR"}`

Runtime log level management:

`/actuator/loggers`

---

## Distributed Tracing

Instrumentation:

- Micrometer Tracing

- OpenTelemetry bridge (`micrometer-tracing-bridge-otel`)

Export:

- Zipkin-compatible format

- Sent to Tempo (internal Docker endpoint on port 9411)

Automatic tracing covers:

- Incoming HTTP requests (UI endpoints)

- Controller → Service boundaries

- Feign client calls to backend services

- Exception handling

- Security filter chain

Trace context propagates to backend services via HTTP headers.

This enables:

- End-to-end request flow visualization:
  UI → Backend API → Database

- Latency breakdown per layer

- Cross-service error analysis

- Log-to-trace correlation via `traceId`

Tempo ingestion endpoint (internal Docker network):

`http://tempo:9411/api/v2/spans`

Trace visualization is performed via Grafana dashboards.

### Tracing Naming Convention

The Admin UI uses the unified tracing naming format: `<domain>/<action>`

**Examples of trace names:**

| Domain | Operation      | Trace Name          |
|--------|----------------|---------------------|
| Wine   | Create         | `wine/create`       |
| Wine   | Find all       | `wine/find-all`     |
| Wine   | Delete         | `wine/delete`       |
| Wine   | Update image   | `wine/update-image` |
| Order  | Find all       | `order/find-all`    |
| Order  | Set paid       | `order/set-paid`    |
| Order  | Delete         | `order/delete`      |
| User   | Update role    | `user/update-role`  |
| Auth   | Login          | `auth/login`        |
| Auth   | Logout         | `auth/logout`       |
| Health | Check          | `health/check`      |
| Health | Init           | `health/init`       |
| UI     | Wine form      | `ui/wine-form`      |
| UI     | Order form     | `ui/order-form`     |
| UI     | User role form | `ui/user-role-form` |
| UI     | Dashboard view | `ui/dashboard-view` |

---

## Key Differences from Backend Service

The Admin UI:

- Does not use a database

- Does not emit JDBC or HikariCP metrics

- Does not perform domain persistence

Observability focuses on:

- HTTP performance

- Feign client latency

- Error propagation

- Role-based access flow

- UI-to-backend trace propagation