#!/bin/sh

# Start services in the correct order
echo "Starting Config Server..."
java -jar /app/config-server.jar &
sleep 15

echo "Starting Discovery Server..."
java -jar /app/discovery-server.jar &
sleep 15

echo "Starting Admin Server..."
java -jar /app/admin-server.jar &
sleep 5

echo "Starting Core Services..."
java -jar /app/customers-service.jar &
java -jar /app/visits-service.jar &
java -jar /app/vets-service.jar &
java -jar /app/genai-service.jar &
sleep 10

echo "Starting API Gateway..."
java -jar /app/api-gateway.jar &

# Keep container running
wait