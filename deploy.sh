#!/bin/bash

./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-admin-server/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-api-gateway/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-config-server/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-customers-service/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-discovery-server/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-vets-service/pom.xml
./mvnw mvn clean install spring-boot:build-image -pl ./spring-petclinic-visits-service/pom.xml





