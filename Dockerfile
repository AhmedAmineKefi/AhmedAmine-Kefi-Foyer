# Build stage (Java 17)
FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage (Java 17)
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install MySQL client for health checks (optional)
RUN apt-get update && \
    apt-get install -y default-mysql-client && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar
COPY src/main/resources/application.properties ./config/

# Expose app + metrics ports
EXPOSE 8086 9404

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/config/application.properties"]
