FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-discovery-server-*.jar discovery-server.jar

EXPOSE 8761

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "discovery-server.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1
