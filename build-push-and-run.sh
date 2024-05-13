#!/bin/bash
./mvnw clean install -P buildDocker

./docker/push-images.sh

docker compose up