#!/bin/bash

echo "Make sure you are logged in to GitHub Container Registry via 'cat ~/GHCR_TOKEN.txt | docker login ghcr.io -u <your_username> --password-stdin'"

docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-api-gateway
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-discovery-server
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-config-server
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-visits-service
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-vets-service
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-customers-service
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/spring-petclinic-admin-server