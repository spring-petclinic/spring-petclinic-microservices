#!/bin/bash

SERVICE_NAME=$1
PORT=$2
IMAGE_TAG=${3:-latest}
NAMESPACE=${4:-dev}

if [ -z "$SERVICE_NAME" ] || [ -z "$PORT" ]; then
  echo "Usage: ./test-local-helm.sh <service-name> <port> [image-tag] [namespace]"
  exit 1
fi

RELEASE_NAME="$SERVICE_NAME-$NAMESPACE"

helm upgrade --install $RELEASE_NAME ./chart \
  --namespace $NAMESPACE \
  --create-namespace \
  --set image.repository="hzeroxium/$SERVICE_NAME" \
  --set image.tag="$IMAGE_TAG" \
  --set service.port=$PORT \
  --set replicas=1 \
  --wait --timeout 180s

kubectl get svc -n $NAMESPACE
