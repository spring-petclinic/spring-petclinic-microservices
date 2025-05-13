FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-genai-service-*.jar genai-service.jar

EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "genai-service.jar"]
