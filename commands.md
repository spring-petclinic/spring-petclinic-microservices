### 1. **Configuración local de Minikube**:
```bash
# Iniciar Minikube con configuración específica (driver Docker, CPUs, memoria, etc.)
minikube start --driver=docker --cpus=4 --memory=4096 --insecure-registry="10.0.0.0/24"

# Obtener la IP de Minikube
minikube ip

# Configurar el entorno Docker para usar la máquina de Minikube
eval $(minikube -p minikube docker-env)


# Reconfigurar el entorno Docker para Minikube
eval $(minikube -p minikube docker-env)
```

### 2. **Generar los Pods en Kubernetes**:
```bash
# Crear los pods a partir de los archivos YAML en el directorio 'k8s/'
kubectl apply -f k8s/ --recursive
```

### 3. **Generar e instalar Linkerd**:
```bash
# Instalar Linkerd
curl -sL https://run.linkerd.io/install | sh

# Agregar el CLI de Linkerd al PATH (si no se hizo en el paso anterior)
export PATH=$PATH:/home/sebastian411/.linkerd2/bin

# Validar la instalación de Linkerd
linkerd check --pre

# Instalar los CRDs (Custom Resource Definitions) de Linkerd
linkerd install --crds | kubectl apply -f -

# Instalar el plano de control de Linkerd
linkerd install | kubectl apply -f -

# Verificar la instalación de Linkerd
linkerd check

# Instalar la extensión Viz de Linkerd para monitoreo (opcional)
linkerd viz install | kubectl apply -f -
linkerd viz check

# Aplicar la inyección de Linkerd a los despliegues existentes
kubectl get deploy -o yaml | linkerd inject - | kubectl apply -f -

# ------------------------------------------
# Comandos de Verificación en Linkerd
# ------------------------------------------

# Verificar los CRDs de Linkerd
kubectl get crds | grep linkerd

# Verificar los ConfigMaps de Linkerd
kubectl get configmap -n linkerd

# Verificar el estado de los pods en Linkerd
kubectl get pods

# Verificar los pods después de la inyección de Linkerd
kubectl get pods -o wide
```


### 4. KEDA

helm repo add kedacore https://kedacore.github.io/charts
helm repo update
helm install keda kedacore/keda --namespace keda --create-namespace
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
helm install prometheus prometheus-community/prometheus --namespace monitoring --create-namespace
kubectl apply --server-side -f https://github.com/kedacore/keda/releases/download/v2.16.0/keda-2.16.0-crds.yaml
kubectl apply -f k8s/ --recursive
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
helm install grafana grafana/grafana --namespace monitoring --create-namespace
kubectl port-forward -n monitoring svc/grafana 3000:80
