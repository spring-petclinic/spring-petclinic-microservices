# spring-petclinic-admin-server/Dockerfile
# ================================
# 🌱 Stage 1: Build and extract layers from Spring Boot executable jar
# ================================
FROM eclipse-temurin:17 AS builder

WORKDIR /application

# Copy the compiled jar (assuming it's already built with mvn package)
ARG ARTIFACT_NAME=spring-petclinic-admin-server-3.4.1
COPY target/${ARTIFACT_NAME}.jar app.jar

# Extract Spring Boot layers for better caching
RUN java -Djarmode=layertools -jar app.jar extract

# ================================
# 🚀 Stage 2: Runtime image with minimal footprint
# ================================
FROM eclipse-temurin:17-jre

WORKDIR /application

# Explicitly set the admin server port to match configuration
ARG EXPOSED_PORT=9090
EXPOSE ${EXPOSED_PORT}

# Set Docker profile to enable proper service discovery configuration
ENV SPRING_PROFILES_ACTIVE=docker

# Copy layers in order of least frequently changed to most frequently changed
# This optimizes rebuilds by leveraging Docker layer caching
COPY --from=builder /application/dependencies/ ./
RUN true  # Creates a new layer to improve caching
COPY --from=builder /application/spring-boot-loader/ ./
RUN true  # Creates a new layer to improve caching
COPY --from=builder /application/snapshot-dependencies/ ./
RUN true  # Creates a new layer to improve caching
COPY --from=builder /application/application/ ./

# Use the Spring Boot JarLauncher directly rather than a shell script
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
