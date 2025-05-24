FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-config-server-*.jar config-server.jar

EXPOSE 8888

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "config-server.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1
