# syntax=docker/dockerfile:1.7
# Multi-stage build so ACR build service (or any vanilla Docker) produces
# a runnable image without needing Java/Gradle pre-installed on the host.
# Replaces the previous single-stage Dockerfile that expected build/libs/*.jar
# to be pre-built locally.
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /src
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-jammy
COPY --from=builder /src/build/libs/*.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
