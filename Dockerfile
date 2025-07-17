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

COPY --from=build /app/target/*.jar app.jar

# Expose app + metrics ports
EXPOSE 8086 9464

ENTRYPOINT ["java", "-jar", "app.jar"]
