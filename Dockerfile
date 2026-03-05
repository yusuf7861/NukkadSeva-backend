# ---- Stage 1: Build ----
FROM eclipse-temurin:17-jdk-alpine AS builder
LABEL authors="yusuf7861"

WORKDIR /app

# Copy Gradle wrapper and build files first (for caching)
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Give execute permission to gradlew
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./gradlew bootJar -x test --no-daemon

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]