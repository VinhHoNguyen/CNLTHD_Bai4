FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache dependencies first for faster incremental builds.
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /build/target/inventory-service-0.0.1-SNAPSHOT.jar /app/inventory-service.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/inventory-service.jar"]