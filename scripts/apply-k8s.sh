#!/bin/bash

# Tạo namespace nếu chưa có
kubectl create namespace petclinic-dev

# Thay thế các biến và áp dụng file cấu hình
DOCKER_USERNAME=fatu29
NAMESPACE=petclinic-dev
TAG=latest

# Áp dụng config-server trước
echo "Applying config-server..."
sed -e "s|\${NAMESPACE}|$NAMESPACE|g" \
    -e "s|\${DOCKER_USERNAME}|$DOCKER_USERNAME|g" \
    -e "s|\${CONFIG_SERVER_TAG}|$TAG|g" \
    k8s/templates/config-server.yaml | kubectl apply -f -

# Đợi config-server khởi động
echo "Waiting for config-server to start..."
sleep 20

# Áp dụng discovery-server
echo "Applying discovery-server..."
sed -e "s|\${NAMESPACE}|$NAMESPACE|g" \
    -e "s|\${DOCKER_USERNAME}|$DOCKER_USERNAME|g" \
    -e "s|\${DISCOVERY_SERVER_TAG}|$TAG|g" \
    k8s/templates/discovery-server.yaml | kubectl apply -f -

# Đợi discovery-server khởi động
echo "Waiting for discovery-server to start..."
sleep 20

# Áp dụng các service còn lại
for service in customers-service vets-service visits-service genai-service api-gateway admin-server; do
  echo "Applying $service..."
  
  # Chuyển đổi tên service thành tên biến TAG
  service_upper=$(echo $service | tr '-' '_' | tr '[:lower:]' '[:upper:]')
  
  # Thay thế các biến và áp dụng
  sed -e "s|\${NAMESPACE}|$NAMESPACE|g" \
      -e "s|\${DOCKER_USERNAME}|$DOCKER_USERNAME|g" \
      -e "s|\${${service_upper}_TAG}|$TAG|g" \
      k8s/templates/$service.yaml | kubectl apply -f -
done

echo "All services deployed to namespace: $NAMESPACE"