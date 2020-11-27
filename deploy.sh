#!/bin/bash

# clean up docker images
docker rmi $(docker images | grep "^<none>" | awk "{print $3}")
docker rmi $(docker images | grep "^spring-petclinic" | awk "{print $3}")

# package applications
./mvnw clean package -pl ./spring-petclinic-admin-server/pom.xml
./mvnw clean package -pl ./spring-petclinic-api-gateway/pom.xml
./mvnw clean package -pl ./spring-petclinic-config-server/pom.xml
./mvnw clean package -pl ./spring-petclinic-customers-service/pom.xml
./mvnw clean package -pl ./spring-petclinic-discovery-server/pom.xml
./mvnw clean package -pl ./spring-petclinic-vets-service/pom.xml
./mvnw clean package -pl ./spring-petclinic-visits-service/pom.xml

docker build . --no-cache -t spring-petclinic-admin-server:1.0 -f ./spring-petclinic-admin-server/Dockerfile
docker build . --no-cache -t spring-petclinic-api-gateway:1.0 -f ./spring-petclinic-api-gateway/Dockerfile
docker build . --no-cache -t spring-petclinic-config-server:1.0 -f ./spring-petclinic-config-server/Dockerfile
docker build . --no-cache -t spring-petclinic-customers-service:1.0 -f ./spring-petclinic-customers-service/Dockerfile
docker build . --no-cache -t spring-petclinic-discovery-server:1.0 -f ./spring-petclinic-discovery-server/Dockerfile
docker build . --no-cache -t spring-petclinic-vets-service:1.0 -f ./spring-petclinic-vets-service/Dockerfile
docker build . --no-cache -t spring-petclinic-visits-service:1.0 -f ./spring-petclinic-visits-service/Dockerfile

# clean up none-labeled docker images
docker rmi $(docker images | grep "^<none>" | awk "{print $3}")

docker-compose up -d





