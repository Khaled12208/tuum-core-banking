FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

RUN gradle :fs-accounts-service:build -x test

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/fs-accounts-service/build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"] 