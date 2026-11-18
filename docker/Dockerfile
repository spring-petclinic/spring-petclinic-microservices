FROM eclipse-temurin:17 AS builder
WORKDIR application
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher --destination extracted


FROM eclipse-temurin:17
WORKDIR application

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}

ENV SPRING_PROFILES_ACTIVE=docker

COPY --from=builder application/extracted/dependencies/ ./

# fix for https://stackoverflow.com/questions/51115856/docker-failed-to-export-image-failed-to-create-image-failed-to-get-layer
# (only last copy caused issue)
# this seems to be triggered by using btrfs:
# https://github.com/moby/moby/issues/36573
RUN true
COPY --from=builder application/extracted/spring-boot-loader/ ./
RUN true
COPY --from=builder application/extracted/snapshot-dependencies/ ./
RUN true
COPY --from=builder application/extracted/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
