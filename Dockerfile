# Build stage
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY config/application.yml ./config/

# Prometheus metrics endpoint
EXPOSE 8080 9404
ENTRYPOINT ["java", "-jar", "app.jar"]
