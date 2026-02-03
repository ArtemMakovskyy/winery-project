# ---------- Stage 1: Build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ---------- Stage 2: Extract ----------
FROM eclipse-temurin:21-jre-jammy AS extractor
WORKDIR /opt/app
COPY --from=build /app/app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ---------- Stage 3: Final ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /application
COPY --from=extractor /opt/app/dependencies/ ./
COPY --from=extractor /opt/app/spring-boot-loader/ ./
COPY --from=extractor /opt/app/snapshot-dependencies/ ./
COPY --from=extractor /opt/app/application/ ./
COPY uploads/images/wine/ /app/uploads/images/wine/

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]