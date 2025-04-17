FROM eclipse-temurin:17 AS builder
WORKDIR application
ARG ARTIFACT_NAME
COPY ${ARTIFACT_NAME}.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM eclipse-temurin:17
WORKDIR application

ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
ENV DOCKERIZE_VERSION=v0.9.3
RUN apt-get update \
    && apt -y upgrade \
    && apt-get install -y wget \
    && wget -O - https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz | tar xzf - -C /usr/local/bin \
    && apt-get autoremove -yqq --purge wget && rm -rf /var/lib/apt/lists/*
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

# Create entrypoint.sh script inline
RUN echo '#!/bin/bash\n\
if [[ "$HOSTNAME" == *"config-server"* ]]; then\n\
  CONFIG_SERVER="localhost:8888"\n\
  echo "Running on the config service"\n\
else\n\
  CONFIG_SERVER="${CONFIG_SERVER:-config-server:8888}"\n\
  echo "Running on a different service"\n\
fi\n\
\n\
if [[ "$SPRING_PROFILES_ACTIVE" == *"k8s"* ]]; then\n\
  echo "Running in k8s, wait for config-server to be ready"\n\
  dockerize -wait http://$CONFIG_SERVER -timeout 60s\n\
else\n\
  echo "Running NOT in k8s, assuming the config server $CONFIG_SERVER is reachable"\n\
fi\n\
echo "Starting Application"\n\
cd /application\n\
java org.springframework.boot.loader.launch.JarLauncher\n' > /entrypoint.sh

# Make the script executable
RUN chmod +x /entrypoint.sh


ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
