FROM alpine:3.20 AS health-checker

ARG TARGETARCH

RUN apk add --no-cache wget xz && \
    if [ "$TARGETARCH" = "arm64" ]; then \
        HEALTH_CHECKER_ARCH="aarch64"; \
    else \
        HEALTH_CHECKER_ARCH="x86_64"; \
    fi && \
    wget -O /tmp/health-checker.tar.xz \
        "https://github.com/dmikusa/tiny-health-checker/releases/download/v0.39.0/tiny-health-checker-${HEALTH_CHECKER_ARCH}-unknown-linux-musl.tar.xz" && \
    mkdir -p /health-checker && \
    tar -xJf /tmp/health-checker.tar.xz -C /tmp && \
    mv /tmp/tiny-health-checker-*/thc /health-checker/thc

FROM eclipse-temurin:17 AS builder
WORKDIR application
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM eclipse-temurin:17
WORKDIR application

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=health-checker /health-checker/thc /workspace/health-check

COPY --from=builder application/dependencies/ ./

# fix for https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
# (only last copy caused issue)
# this seems to be triggered by using btrfs:
# https://github.com/moby/moby/issues/36573
RUN true
COPY --from=builder application/spring-boot-loader/ ./
RUN true
COPY --from=builder application/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
