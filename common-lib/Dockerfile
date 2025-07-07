FROM gradle:8.5-jdk17 AS builder

WORKDIR /app
COPY . .

RUN gradle :common-lib:build -x test

FROM openjdk:17-jre-slim

WORKDIR /app

COPY --from=builder /app/common-lib/build/libs/*.jar /app/common-lib/

CMD ["echo", "Common library built successfully"] 