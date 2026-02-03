FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/sample-resource-server-1.0.0-MVP.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
