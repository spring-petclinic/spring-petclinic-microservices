#!/bin/bash

# Create log directory for Spring Petclinic services
mkdir -p /var/log/spring-petclinic

# Create log files for each service
touch /var/log/spring-petclinic/api-gateway.log
touch /var/log/spring-petclinic/customers-service.log
touch /var/log/spring-petclinic/visits-service.log
touch /var/log/spring-petclinic/vets-service.log

# Set appropriate permissions
chmod 666 /var/log/spring-petclinic/*.log

echo "Log directories and files have been created."