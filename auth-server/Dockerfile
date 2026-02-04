# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Gradle wrapper and project files
COPY gradlew gradle/wrapper/gradle-wrapper.jar ./
COPY auth-server/build.gradle ./build.gradle
COPY auth-server/src ./src

# Build
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
