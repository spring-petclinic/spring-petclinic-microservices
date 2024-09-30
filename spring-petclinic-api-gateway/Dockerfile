FROM  --platform=amd64 eclipse-temurin:17 AS builder
WORKDIR /application
ARG ARTIFACT_NAME
ARG DOCKERIZE_VERSION
COPY target/${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

# Download dockerize and cache that layer
ARG DOCKERIZE_VERSION
RUN wget -O dockerize.tar.gz https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize

FROM  --platform=amd64 eclipse-temurin:17

WORKDIR /application

# Dockerize
COPY --from=builder /application/dockerize ./

RUN mkdir -p /opt/otel
#WORKDIR /opt/otel
#RUN curl -L https://github.com/signalfx/splunk-otel-java/releases/download/v1.30.0/splunk-otel-javaagent-all.jar -o splunk-otel-javaagent-all.jar

ARG EXPOSED_PORT
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

WORKDIR /application
COPY --from=builder /application/dependencies/ ./
COPY --from=builder /application/spring-boot-loader/ ./
COPY --from=builder /application/snapshot-dependencies/ ./
COPY --from=builder /application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
