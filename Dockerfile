# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files first (for better caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle* ./
COPY gradle.properties* ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer will be cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre

# Install curl for health checks and create necessary directories
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /app/logs /app/uploads

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to app user
RUN chown -R appuser:appuser /app
USER appuser

# Expose ports (8080 for HTTP, 9090 for gRPC)
EXPOSE 8080 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

