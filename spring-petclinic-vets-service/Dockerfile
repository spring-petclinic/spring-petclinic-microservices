FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-vets-service-*.jar vets-service.jar

EXPOSE 8083

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "vets-service.jar"]
