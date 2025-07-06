# Multi-stage build for cs-accounts-events-consumer
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

# Build the application
RUN gradle :cs-accounts-events-consumer:build -x test

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/cs-accounts-events-consumer/build/libs/*.jar app.jar

# Install curl for health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 