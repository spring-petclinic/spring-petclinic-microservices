# ───────────────────────────────────────
# Stage 1: Build specific microservice
# ───────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY . .

ARG SERVICE_NAME

RUN mvn -f pom.xml dependency:go-offline
RUN mvn -f pom.xml clean package -DskipTests -pl ${SERVICE_NAME} -am

# ───────────────────────────────────────
# Stage 2: Runtime
# ───────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app
VOLUME /tmp

ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
