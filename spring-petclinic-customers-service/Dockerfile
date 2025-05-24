FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-customers-service-*.jar customers-service.jar

EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "customers-service.jar"]
