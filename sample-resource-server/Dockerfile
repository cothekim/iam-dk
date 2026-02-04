# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Install Gradle
RUN apk add --no-cache gradle

# Copy project files
COPY sample-resource-server/build.gradle ./build.gradle
COPY sample-resource-server/src ./src

# Build
RUN gradle build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
