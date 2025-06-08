#!/bin/bash

# Đăng nhập vào Docker Hub
docker login

# Di chuyển đến thư mục gốc của dự án
cd /home/fat/code/devops/spring-petclinic-microservices

# Build ứng dụng với Maven
./mvnw clean package -DskipTests

# Build và push Docker images
export DOCKER_USERNAME=fatu29

# Build và push từng service
for service in config-server discovery-server api-gateway customers-service vets-service visits-service genai-service admin-server; do
  echo "Building and pushing $service..."
  
  # Di chuyển đến thư mục service
  cd spring-petclinic-$service
  
  # Tạo Dockerfile nếu chưa có
  if [ ! -f Dockerfile ]; then
    echo "FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/*.jar app.jar
ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]" > Dockerfile
  fi
  
  # Build Docker image
  docker build -t $DOCKER_USERNAME/petclinic-$service:latest .
  
  # Push lên Docker Hub
  docker push $DOCKER_USERNAME/petclinic-$service:latest
  
  # Quay lại thư mục gốc
  cd ..
done

echo "All Docker images have been built and pushed to Docker Hub!"
