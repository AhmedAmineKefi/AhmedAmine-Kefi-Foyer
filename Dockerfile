# Build stage (Java 17)
FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage (Java 17)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Install MySQL client for health checks (optional)
RUN apt-get update && \
    apt-get install -y default-mysql-client && \
    rm -rf /var/lib/apt/lists/*

ARG OTEL_AGENT_VERSION=1.32.0
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar /otel-agent.jar
ENV JAVA_TOOL_OPTIONS "-javaagent:/otel-agent.jar"

COPY --from=build /app/target/*.jar app.jar

# Expose app + metrics ports
EXPOSE 8086 9404

# Use environment variables to override application.properties
ENTRYPOINT ["java", "-jar", "app.jar"]
