# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy pre-built JAR from local build
COPY auth-server/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
