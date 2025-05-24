FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-visits-service-*.jar visits-service.jar

EXPOSE 8082

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "visits-service.jar"]
