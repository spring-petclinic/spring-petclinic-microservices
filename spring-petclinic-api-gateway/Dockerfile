FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-api-gateway-*.jar api-gateway.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "api-gateway.jar"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
