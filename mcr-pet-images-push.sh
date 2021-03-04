#! /bin/bash

ACTUAL_VERSION="0.3.0"

echo "chagne latest tag to $ACTUAL_VERSION"

docker image tag adenmin/spring-petclinic-config-server:latest adenmin/spring-petclinic-config-server:$ACTUAL_VERSION
docker image tag adenmin/spring-petclinic-discovery-server:latest adenmin/spring-petclinic-discovery-server:$ACTUAL_VERSION
docker image tag adenmin/spring-petclinic-customers-service:latest adenmin/spring-petclinic-customers-service:$ACTUAL_VERSION
docker image tag adenmin/spring-petclinic-visits-service:latest adenmin/spring-petclinic-visits-service:$ACTUAL_VERSION
docker image tag adenmin/spring-petclinic-vets-service:latest adenmin/spring-petclinic-vets-service:$ACTUAL_VERSION
docker image tag adenmin/spring-petclinic-api-gateway:latest adenmin/spring-petclinic-api-gateway:$ACTUAL_VERSION

echo "push images"

docker image push adenmin/spring-petclinic-config-server:$ACTUAL_VERSION
docker image push adenmin/spring-petclinic-discovery-server:$ACTUAL_VERSION
docker image push adenmin/spring-petclinic-customers-service:$ACTUAL_VERSION
docker image push adenmin/spring-petclinic-visits-service:$ACTUAL_VERSION
docker image push adenmin/spring-petclinic-vets-service:$ACTUAL_VERSION
docker image push adenmin/spring-petclinic-api-gateway:$ACTUAL_VERSION

echo "vseo!"
