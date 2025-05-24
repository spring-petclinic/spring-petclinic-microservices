#!/bin/bash

# Script để tạo tất cả Dockerfile trong đúng thư mục

echo "Creating Dockerfiles for all services..."

# Admin Server
mkdir -p spring-petclinic-admin-server
cat > spring-petclinic-admin-server/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-admin-server-*.jar admin-server.jar

EXPOSE 9090

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "admin-server.jar"]
DOCKER_EOF

# API Gateway
mkdir -p spring-petclinic-api-gateway
cat > spring-petclinic-api-gateway/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-api-gateway-*.jar api-gateway.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
DOCKER_EOF

# Config Server
mkdir -p spring-petclinic-config-server
cat > spring-petclinic-config-server/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-config-server-*.jar config-server.jar

EXPOSE 8888

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "config-server.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1
DOCKER_EOF

# Customers Service
mkdir -p spring-petclinic-customers-service
cat > spring-petclinic-customers-service/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-customers-service-*.jar customers-service.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "customers-service.jar"]
DOCKER_EOF

# Discovery Server
mkdir -p spring-petclinic-discovery-server
cat > spring-petclinic-discovery-server/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-discovery-server-*.jar discovery-server.jar

EXPOSE 8761

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "discovery-server.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1
DOCKER_EOF

# GenAI Service
mkdir -p spring-petclinic-genai-service
cat > spring-petclinic-genai-service/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-genai-service-*.jar genai-service.jar

EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "genai-service.jar"]
DOCKER_EOF

# Vets Service
mkdir -p spring-petclinic-vets-service
cat > spring-petclinic-vets-service/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-vets-service-*.jar vets-service.jar

EXPOSE 8083

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "vets-service.jar"]
DOCKER_EOF

# Visits Service
mkdir -p spring-petclinic-visits-service
cat > spring-petclinic-visits-service/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-visits-service-*.jar visits-service.jar

EXPOSE 8082

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "visits-service.jar"]
DOCKER_EOF

echo "✅ All Dockerfiles created successfully!"
echo ""
echo "Created files:"
echo "- spring-petclinic-admin-server/Dockerfile"
echo "- spring-petclinic-api-gateway/Dockerfile"
echo "- spring-petclinic-config-server/Dockerfile"
echo "- spring-petclinic-customers-service/Dockerfile"
echo "- spring-petclinic-discovery-server/Dockerfile"
echo "- spring-petclinic-genai-service/Dockerfile"
echo "- spring-petclinic-vets-service/Dockerfile"
echo "- spring-petclinic-visits-service/Dockerfile"
