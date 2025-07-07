FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

RUN gradle :cs-accounts-events-consumer:build -x test

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/cs-accounts-events-consumer/build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"] 