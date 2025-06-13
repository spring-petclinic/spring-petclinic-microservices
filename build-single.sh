#!/bin/bash

# Source credentials if needed
if [ -f .env ]; then
    source .env
fi

# Set Docker Hub username or use default
DOCKER_HUB_USERNAME=${DOCKER_HUB_USERNAME:-"local"}

echo "🚀 Building all services..."
./mvnw clean package -DskipTests

# Check if all JARs were built
SERVICES=(
    "spring-petclinic-admin-server"
    "spring-petclinic-api-gateway"
    "spring-petclinic-config-server"
    "spring-petclinic-customers-service"
    "spring-petclinic-discovery-server"
    "spring-petclinic-genai-service"
    "spring-petclinic-vets-service"
    "spring-petclinic-visits-service"
)

ALL_BUILT=true
for service in "${SERVICES[@]}"; do
    if [ ! -f "$service/target/"*.jar ]; then
        echo "❌ Missing JAR for $service"
        ALL_BUILT=false
    fi
done

if [ "$ALL_BUILT" = false ]; then
    echo "❌ Not all services built successfully. Exiting."
    exit 1
fi

echo "🐳 Building monolithic Docker image..."
docker build -t $DOCKER_HUB_USERNAME/spring-petclinic-all:latest -f Dockerfile.all .

if [ $? -eq 0 ]; then
    echo "✅ Successfully built combined image: $DOCKER_HUB_USERNAME/spring-petclinic-all:latest"
    
    # Push to Docker Hub if credentials are available
    if [ ! -z "$DOCKER_HUB_TOKEN" ]; then
        echo "🔐 Logging into Docker Hub..."
        echo $DOCKER_HUB_TOKEN | docker login -u $DOCKER_HUB_USERNAME --password-stdin
        
        echo "📤 Pushing to Docker Hub..."
        docker push $DOCKER_HUB_USERNAME/spring-petclinic-all:latest
    fi
else
    echo "❌ Failed to build image"
fi