package com.winestoreapp;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class Application {

    @Value("${main.back.end.url}")
    private String mainFrontEndUrl;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void logUrls() {
        log.info("");
        log.info("🍷 ================================================================");
        log.info("🍷  Stoic Winery API - Backend");
        log.info("🍷  Modular Monolith | Spring Boot 3 | Java 21");
        log.info("🍷 ================================================================");
        log.info("");

        log.info("🔎 Health check:");
        log.info("   ➜ {}{}", mainFrontEndUrl, "/health");

        log.info("");
        log.info("📚 API Documentation (Swagger):");
        log.info("   ➜ {}/swagger-ui/index.html#/", mainFrontEndUrl);

        log.info("");
        log.info("🖥️ Frontend:");
        log.info("   ➜ http://localhost:3000/#/products");

        log.info("");
        log.info("📊 Observability & Monitoring:");

        log.info("   📈 Grafana Dashboards:");
        log.info("      ➜ http://localhost:3030");

        log.info("   📊 Prometheus Metrics:");
        log.info("      ➜ http://localhost:9090");

        log.info("   🧾 Logs (Loki via Grafana):");
        log.info("      ➜ Grafana → Explore → Loki");
        log.info("      ➜ Query example: {app=\"wine-store-api\"}");

        log.info("   🧭 Traces (Tempo):");
        log.info("      ➜ Grafana → Traces → Tempo");

        log.info("");
        log.info("👤 Default Users:");
        log.info("   ➜ ADMIN    | admin12345@gmail.com / 12345");
        log.info("   ➜ MANAGER  | manager12345@gmail.com / 12345");

        log.info("");
        log.info("📦 Source code:");
        log.info("   ➜ https://github.com/ArtemMakovskyy/wine-store-app");

        log.info("");
        log.info("🍷 ================================================================");
        log.info("🍷  Application started successfully");
        log.info("🍷 ================================================================");
        log.info("");
    }
}
