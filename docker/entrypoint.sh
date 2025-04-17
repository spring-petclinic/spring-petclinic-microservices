#!/bin/bash
if [[ "$HOSTNAME" == *"config-server"* ]]; then
  CONFIG_SERVER="localhost:8888"
  echo "Running on the config service"
else
  CONFIG_SERVER="${CONFIG_SERVER:-config-server:8888}"
  echo "Running on a different service"
fi

if [[ "$SPRING_PROFILES_ACTIVE" == *"k8s"* ]]; then
    echo "Running in k8s, wait for config-server to be ready"
    dockerize -wait http://$CONFIG_SERVER -timeout 60s
else
    echo "Running NOT in k8s, assuming the config server $CONFIG_SERVER is reachable"
fi
echo "Starting Application"
cd /application
java org.springframework.boot.loader.launch.JarLauncher