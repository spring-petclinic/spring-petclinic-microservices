#!/bin/bash

# Array of Docker container names
containers=("spring-petclinic-api-gateway" "spring-petclinic-discovery-server" "spring-petclinic-config-server" "spring-petclinic-visits-service" "spring-petclinic-vets-service" "spring-petclinic-customers-service" "spring-petclinic-admin-server")

# Docker registry URL
registry="quay.io"

# Docker repository namespace
namespace="phagen"

# Check if a tag was provided as an argument
if [ $# -eq 0 ]; then
    echo "No tag provided. Using 'latest' as the default tag."
    tag="latest"
else
    tag="$1"
fi

# Loop through each container and push it to the registry
for container in "${containers[@]}"
do
    echo "Tagging $container with tag $tag..."
    
    # Tag the Docker container
    docker tag "$container" "$registry/$namespace/$container:$tag"

    # Check if tagging was successful
    if [ $? -ne 0 ]; then
        echo "Failed to tag $container with tag $tag. Exiting..."
        exit 1
    fi

    echo "Pushing $container with tag $tag to $registry/$namespace..."

    # Push the Docker container with the specified tag
    docker push "$registry/$namespace/$container:$tag"

    # Check the push result
    if [ $? -eq 0 ]; then
        echo "Successfully pushed $container with tag $tag"
        echo "Removing local $container image..."
        docker rmi "$registry/$namespace/$container:$tag"
    else
        echo "Failed to push $container with tag $tag"
        exit 1
    fi
done

docker images