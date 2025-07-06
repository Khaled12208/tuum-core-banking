FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

# Build the common-lib module
RUN gradle :common-lib:build -x test

# Create a stage for the built artifacts
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the built common-lib JAR
COPY --from=builder /app/common-lib/build/libs/*.jar /app/common-lib/

# This is a library module, so we don't need to run anything
# The JAR will be available for other modules to use
CMD ["echo", "Common library built successfully"] 