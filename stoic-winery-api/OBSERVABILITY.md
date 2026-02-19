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