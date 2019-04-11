FROM maven:3.6.0-jdk-8 AS builder

COPY . .
RUN mvn clean package

FROM openjdk:8-jre-alpine AS spring-petclinic-admin-server
VOLUME /tmp
ARG DOCKERIZE_VERSION
ARG ARTIFACT_NAME
ARG EXPOSED_PORT
ENV SPRING_PROFILES_ACTIVE docker

ADD https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz dockerize.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize
COPY --from=builder /spring-petclinic-admin-server/target/${ARTIFACT_NAME}.jar /app.jar
#ADD ${ARTIFACT_NAME}.jar /app.jar
RUN touch /app.jar
EXPOSE ${EXPOSED_PORT}
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
