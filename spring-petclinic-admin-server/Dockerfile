FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-admin-server-*.jar admin-server.jar

EXPOSE 9090

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "admin-server.jar"]
