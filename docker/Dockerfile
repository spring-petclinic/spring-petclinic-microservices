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
# ✅ Ajout d'un fichier avec la date pour forcer une mise à jour détectable
RUN echo "🚀 Nouvelle version déployée le 28 mars" > /version.txt
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
