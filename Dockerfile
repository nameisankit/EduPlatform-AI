# ────────────────────────────────────────────────────────────────
# EduPlatform AI — Dockerfile (Spring Boot)
# ────────────────────────────────────────────────────────────────

# Stage 1: Build with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Run (slim JRE image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user (security best practice)
RUN addgroup -S eduplatform && adduser -S eduplatform -G eduplatform
USER eduplatform

COPY --from=build /app/target/eduplatform-ai-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
