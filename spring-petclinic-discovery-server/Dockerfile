ARG BASE_ID

FROM builder:${BASE_ID} as src

FROM base:${BASE_ID}

VOLUME /tmp
ARG REVISION
ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
COPY --from=src --chown=1000:0 /spring-petclinic-discovery-server/target/spring-petclinic-discovery-server-${REVISION}.jar /app.jar
RUN touch /app.jar
