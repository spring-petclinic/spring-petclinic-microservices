FROM openjdk:8-jdk-alpine
MAINTAINER Hazar Gul Nazari
ARG JAR_FILE=./spring-petclinic-customers-service/target/spring-petclinic-customers-service-1.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
