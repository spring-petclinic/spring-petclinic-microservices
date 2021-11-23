#! /bin/bash

ACTUAL_VERSION="0.4.0"

echo "chagne latest tag to $ACTUAL_VERSION"

docker image tag drill4j/spring-petclinic-config-server:latest drill4j/spring-petclinic-config-server:$ACTUAL_VERSION
docker image tag drill4j/spring-petclinic-discovery-server:latest drill4j/spring-petclinic-discovery-server:$ACTUAL_VERSION
docker image tag drill4j/spring-petclinic-customers-service:latest drill4j/spring-petclinic-customers-service:$ACTUAL_VERSION
docker image tag drill4j/spring-petclinic-visits-service:latest drill4j/spring-petclinic-visits-service:$ACTUAL_VERSION
docker image tag drill4j/spring-petclinic-vets-service:latest drill4j/spring-petclinic-vets-service:$ACTUAL_VERSION
docker image tag drill4j/spring-petclinic-api-gateway:latest drill4j/spring-petclinic-api-gateway:$ACTUAL_VERSION

echo "push images"

docker image push drill4j/spring-petclinic-config-server:$ACTUAL_VERSION
docker image push drill4j/spring-petclinic-discovery-server:$ACTUAL_VERSION
docker image push drill4j/spring-petclinic-customers-service:$ACTUAL_VERSION
docker image push drill4j/spring-petclinic-visits-service:$ACTUAL_VERSION
docker image push drill4j/spring-petclinic-vets-service:$ACTUAL_VERSION
docker image push drill4j/spring-petclinic-api-gateway:$ACTUAL_VERSION

echo "vseo!"
