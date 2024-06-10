#!/bin/bash

docker build -t ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/make-it-crash-be ./make-it-crash-be
docker build -t ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/make-it-crash-fe ./make-it-crash-fe

docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/make-it-crash-be
docker push ghcr.io/ivan-bobrov/make-spring-petclinic-microservices-crash/make-it-crash-fe

docker-compose up
