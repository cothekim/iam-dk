# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy pre-built JAR from local build
COPY directory-service/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
