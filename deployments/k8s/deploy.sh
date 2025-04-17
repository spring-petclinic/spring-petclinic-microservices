#!/bin/bash
# Define the namespace
: "${NAMESPACE:=petclinic}"

# Check if the namespace exists
if kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
    echo "Namespace '$NAMESPACE' already exists. Delete existing resources"
    kubectl -n $NAMESPACE delete configmap petclinic-repo
    kubectl -n $NAMESPACE delete configmap petclinic-db-cm0
else
    echo "Namespace '$NAMESPACE' does not exist. Creating..."
    kubectl create namespace "$NAMESPACE"
    echo "Namespace '$NAMESPACE' created."
fi

#Create ConfigMaps for DB and config-server
kubectl -n $NAMESPACE create configmap petclinic-repo --from-file=../config/spring-petclinic-microservices-config
kubectl -n $NAMESPACE create configmap petclinic-db-cm0 --from-file=../config/db

kubectl -n $NAMESPACE apply -f deployments/petclinic-db-deployment.yaml 
kubectl -n $NAMESPACE apply -f deployments/petclinic-db-service.yaml
kubectl -n $NAMESPACE apply -f deployments/config-server-deployment.yaml
kubectl -n $NAMESPACE apply -f deployments/config-server-service.yaml
echo "Waiting for db and configserver"
kubectl -n $NAMESPACE wait --for=condition=available --timeout=600s deployment/petclinic-db
kubectl -n $NAMESPACE wait --for=condition=available --timeout=600s deployment/config-server
# starting the rest - the already started will be ignored as not changed


kubectl -n $NAMESPACE apply -f deployments/