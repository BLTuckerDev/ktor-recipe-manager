# Multi-stage build for Kotlin Ktor application
# Stage 1: Build the application
FROM gradle:8-jdk17 AS build

WORKDIR /app

# Copy gradle files first for better caching
COPY gradle gradle
COPY gradlew .
COPY gradlew.bat .
COPY gradle.properties* ./
COPY settings.gradle.kts* ./
COPY build.gradle.kts .
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Download dependencies (this layer will be cached if dependencies don't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN gradle buildFatJar --no-daemon

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks (useful for Cloud Run)
RUN apk add --no-cache curl

# Create a non-root user to run the application
RUN addgroup -g 1000 ktor && \
    adduser -D -u 1000 -G ktor ktor

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*-all.jar app.jar

# Change ownership to the non-root user
RUN chown -R ktor:ktor /app

# Switch to non-root user
USER ktor

# Google Cloud Run will set the PORT environment variable
# Default to 8080 if not set
ENV PORT=8080
ENV HOST=0.0.0.0

# Expose the port
EXPOSE 8080

# Health check endpoint (adjust the path if your app has a different health endpoint)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:${PORT}/health || exit 1

# Run the application
# JVM options optimized for containerized environments
ENTRYPOINT ["java", \
    "-server", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
