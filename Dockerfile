FROM maven:3.6.0-jdk-8 AS builder
ARG SKIP_TESTS="false" 
ARG REVISION

COPY . .
RUN mvn clean package -Drevision=${REVISION} -DskipTests=${SKIP_TESTS}

FROM openjdk:8-jre-alpine AS base
VOLUME /tmp
ARG DOCKERIZE_VERSION="v0.6.1"
ARG EXPOSED_PORT
ENV SPRING_PROFILES_ACTIVE docker

ADD https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz dockerize.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize

EXPOSE ${EXPOSED_PORT}
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]

FROM base AS spring-petclinic-admin-server
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-admin-server/target/spring-petclinic-admin-server-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-customers-service
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-customers-service/target/spring-petclinic-customers-service-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-vets-service
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-vets-service/target/spring-petclinic-vets-service-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-visits-service
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-visits-service/target/spring-petclinic-visits-service-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-config-server
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-config-server/target/spring-petclinic-config-server-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-discovery-server
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-discovery-server/target/spring-petclinic-discovery-server-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-api-gateway
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-api-gateway/target/spring-petclinic-api-gateway-${REVISION}.jar /app.jar
RUN touch /app.jar

FROM base AS spring-petclinic-hystrix-dashboard
VOLUME /tmp
ARG REVISION
COPY --from=builder /spring-petclinic-hystrix-dashboard/target/spring-petclinic-hystrix-dashboard-${REVISION}.jar /app.jar
RUN touch /app.jar

