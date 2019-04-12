ARG BASE_ID

FROM builder:${BASE_ID} as src

FROM base:${BASE_ID}

VOLUME /tmp
ARG REVISION
ARG EXPOSED_PORT
EXPOSE ${EXPOSED_PORT}
COPY --from=src --chown=1000:0 /spring-petclinic-vets-service/target/spring-petclinic-vets-service-${REVISION}.jar /app.jar
RUN touch /app.jar
