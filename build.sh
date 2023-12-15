#!/bin/bash

# Define directories where Java applications are located
directories=(
    "./spring-petclinic-admin-server"
    "./spring-petclinic-api-gateway"
    "./spring-petclinic-config-server"
    "./spring-petclinic-discovery-server"
    "./spring-petclinic-vets-service"
    "./spring-petclinic-visits-service"
)

# Build Java applications in each directory sequentially
for dir in "${directories[@]}"; do
    if [ -d "$dir" ]; then
        echo "Building Java application in $dir"
        cd "$dir" || exit 1
        mvn clean install  # Run Maven build commands specific to your project
        cd ..
    else
        echo "Directory $dir does not exist."
    fi
done