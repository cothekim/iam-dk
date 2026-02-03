# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy pre-built JAR from local build
COPY sample-resource-server/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
