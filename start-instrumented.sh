#!/bin/bash

curl -vsSL -o ./otel/splunk-otel-javaagent-all.jar 'https://github.com/signalfx/splunk-otel-java/releases/latest/download/splunk-otel-javaagent-all.jar'
./mvnw clean install -P buildDocker
docker-compose up
