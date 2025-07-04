FROM openjdk:17-jdk-slim

WORKDIR /app

COPY cs-accounts-events-consumer/build/libs/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"] 