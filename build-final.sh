#!/bin/bash

# Source credentials
source .env

# Login to Docker Hub
echo "🔐 Logging into Docker Hub..."
echo $DOCKER_HUB_TOKEN | docker login -u $DOCKER_HUB_USERNAME --password-stdin

# Services array
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

# Success counter
SUCCESS_COUNT=0
TOTAL_COUNT=${#SERVICES[@]}

echo ""
echo "🏗️ Starting build process for $TOTAL_COUNT services..."
echo ""

# Build and push each service
for service in "${SERVICES[@]}"; do
    echo "==============================================="
    echo "🔨 Processing: $service"
    echo "==============================================="
    
    if [ -d "$service" ]; then
        cd $service
        
        # Check if JAR exists
        if [ ! -f target/*.jar ]; then
            echo "📦 Building Maven project..."
            mvn clean package -DskipTests
            
            if [ ! -f target/*.jar ]; then
                echo "❌ Maven build failed for $service"
                cd ..
                continue
            fi
        fi
        
        # List JAR file
        JAR_FILE=$(ls target/*.jar | head -1)
        echo "✅ Found JAR: $(basename $JAR_FILE)"
        
        # Build Docker image
        echo "🐳 Building Docker image..."
        docker build -t $DOCKER_HUB_USERNAME/$service:latest .
        
        if [ $? -eq 0 ]; then
            # Check image size
            IMAGE_SIZE=$(docker images $DOCKER_HUB_USERNAME/$service:latest --format "{{.Size}}")
            echo "✅ Build successful - Size: $IMAGE_SIZE"
            
            # Push to Docker Hub
            echo "📤 Pushing to Docker Hub..."
            docker push $DOCKER_HUB_USERNAME/$service:latest
            
            if [ $? -eq 0 ]; then
                echo "✅ Successfully pushed $service"
                ((SUCCESS_COUNT++))
            else
                echo "❌ Failed to push $service"
            fi
        else
            echo "❌ Docker build failed for $service"
        fi
        
        cd ..
        echo ""
    fi
done

echo "==============================================="
echo "🎉 Build Summary"
echo "==============================================="
echo "✅ Successful: $SUCCESS_COUNT/$TOTAL_COUNT services"
echo ""
echo "Your Docker Hub repositories:"
for service in "${SERVICES[@]}"; do
    echo "- https://hub.docker.com/r/$DOCKER_HUB_USERNAME/$service"
done
echo ""

# Show final images
echo "📦 Local images:"
docker images | grep $DOCKER_HUB_USERNAME
