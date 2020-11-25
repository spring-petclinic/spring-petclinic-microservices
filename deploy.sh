#!/bin/bash

echo "====================================="
echo "====  Package Admin Server  ========="
echo "====================================="

./mvnw clean package -pl ./spring-petclinic-admin-server/pom.xml


echo "====================================="
echo "====  Package Api Gateway   ========="
echo "====================================="

./mvnw clean package -pl ./spring-petclinic-api-gateway/pom.xml

echo "======================================="
echo "====  Package Config Server   ========="
echo "======================================="

./mvnw clean package -pl ./spring-petclinic-config-server/pom.xml

echo "==========================================="
echo "====  Package customers service   ========="
echo "==========================================="

./mvnw clean package -pl ./spring-petclinic-customers-service/pom.xml

echo "==========================================="
echo "====  Package discovery server    ========="
echo "==========================================="

./mvnw clean package -pl ./spring-petclinic-discovery-server/pom.xml

echo "======================================="
echo "====  Package Vets Service    ========="
echo "======================================="

./mvnw clean package -pl ./spring-petclinic-vets-service/pom.xml

echo "========================================="
echo "====  Package Visits Service    ========="
echo "========================================="

./mvnw clean package -pl ./spring-petclinic-visits-service/pom.xml



