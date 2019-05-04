FROM openjdk:11-jre
VOLUME /tmp
ARG DOCKERIZE_VERSION
ARG ARTIFACT_NAME
ARG EXPOSED_PORT
ENV SPRING_PROFILES_ACTIVE docker

ADD https://github.com/jwilder/dockerize/releases/download/${DOCKERIZE_VERSION}/dockerize-alpine-linux-amd64-${DOCKERIZE_VERSION}.tar.gz dockerize.tar.gz
RUN tar xzf dockerize.tar.gz
RUN chmod +x dockerize
ADD ${ARTIFACT_NAME}.jar /app.jar
RUN touch /app.jar
EXPOSE ${EXPOSED_PORT}
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
