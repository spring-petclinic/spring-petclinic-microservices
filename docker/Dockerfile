FROM openjdk:8-jre-alpine
VOLUME /tmp

# Download dockerize and cache that layer
ARG DOCKERIZE_VERSION
RUN wget -O dockerize.tar.gz https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize
RUN wget -O opentelemetry-auto-0.2.2.jar https://github.com/open-telemetry/opentelemetry-auto-instr-java/releases/download/v0.2.2/opentelemetry-auto-0.2.2.jar
RUN wget -O opentelemetry-auto-exporters-jaeger-0.2.2.jar https://github.com/open-telemetry/opentelemetry-auto-instr-java/releases/download/v0.2.2/opentelemetry-auto-exporters-jaeger-0.2.2.jar

# This is the first layer that won't be cached
ARG ARTIFACT_NAME
ADD ${ARTIFACT_NAME}.jar /app.jar
RUN wget -O /opt/opentelemetry-auto-0.2.2.jar https://github.com/open-telemetry/opentelemetry-auto-instr-java/releases/download/v0.2.2/opentelemetry-auto-0.2.2.jar
RUN wget -O /opt/opentelemetry-auto-exporters-jaeger-0.2.2.jar https://github.com/open-telemetry/opentelemetry-auto-instr-java/releases/download/v0.2.2/opentelemetry-auto-exporters-jaeger-0.2.2.jar

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

ENV SPRING_PROFILES_ACTIVE docker

ENTRYPOINT ["java", \
            "-javaagent:/opt/opentelemetry-auto-0.2.2.jar", \
            "-Dota.exporter.jar=/opt/opentelemetry-auto-exporters-jaeger-0.2.2.jar", \
            "-Dota.exporter.jaeger.endpoint=otel-collector:14250", \
            "-Dota.exporter.jaeger.service.name=config", \
            "-XX:+UnlockExperimentalVMOptions", \
            "-XX:+UseCGroupMemoryLimitForHeap", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app.jar" \
]
