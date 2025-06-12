#!/bin/bash

echo "Starting Spring Petclinic Microservices Monitoring Stack..."

# Check if running on Windows
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
  # Windows system
  echo "Windows system detected. Setting up logs..."
  powershell -ExecutionPolicy Bypass -File "./scripts/setup-logs-windows.ps1"
else
  # Unix-like system
  echo "Unix-like system detected. Setting up logs..."
  # Ensure log directories exist
  if [ ! -d "/var/log/spring-petclinic" ]; then
    echo "Creating log directories..."
    ./scripts/setup-logs.sh
  fi
fi

# Start the services with docker-compose
echo "Starting services with docker-compose..."
docker-compose up -d prometheus-server loki promtail grafana-server tracing-server

echo "Monitoring stack started successfully!"
echo ""
echo "Access points:"
echo "- Grafana: http://localhost:3030"
echo "- Prometheus: http://localhost:9091"
echo "- Zipkin: http://localhost:9411"
echo ""
echo "See monitoring-README.md for more details on using these tools."