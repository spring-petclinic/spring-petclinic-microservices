FROM java:8
VOLUME /tmp
ARG ARTIFACT_NAME
ADD ${ARTIFACT_NAME}.jar /app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8888
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
