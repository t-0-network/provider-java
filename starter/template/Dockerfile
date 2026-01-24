# Build stage
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy gradle files first for better caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src
COPY libs ./libs

# Build the application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built application
COPY --from=build /app/build/libs/*.jar app.jar

# Copy .env file if it exists (for local development)
COPY .env* ./

# Expose the provider server port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
