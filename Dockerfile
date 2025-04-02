# Dockerfile
# ================================
# 🌱 Stage 1: Build and extract layers from Spring Boot executable jar
# ================================
FROM eclipse-temurin:17 AS builder

WORKDIR /application

# Parameterize the artifact name and service name
ARG SERVICE_NAME
ARG VERSION=3.4.1
ARG ARTIFACT_NAME=spring-petclinic-${SERVICE_NAME}-${VERSION}

# Copy the pre-built jar - look in service subdirectory
COPY spring-petclinic-${SERVICE_NAME}/target/${ARTIFACT_NAME}.jar app.jar

# Extract Spring Boot layers
RUN java -Djarmode=layertools -jar app.jar extract

# ================================
# 🚀 Stage 2: Runtime image 
# ================================
FROM eclipse-temurin:17-jre

WORKDIR /application

# Parameterize the port (each service has its own port)
ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

# Common to all services
ENV SPRING_PROFILES_ACTIVE=docker

# Copy layers in optimal order for caching
COPY --from=builder /application/dependencies/ ./
RUN true
COPY --from=builder /application/spring-boot-loader/ ./
RUN true
COPY --from=builder /application/snapshot-dependencies/ ./
RUN true
COPY --from=builder /application/application/ ./

# Standard entrypoint for all services
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
