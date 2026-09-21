# Kubernetes manifests

Plain manifests, applied in filename order. No Helm, no Kustomize overlays.

| File | Contents |
|---|---|
| `00-namespace.yaml` | the `petclinic` namespace |
| `01-configmaps.yaml` | one shared ConfigMap plus one per service; mirrors [`../config`](../config) |
| `02-secrets.yaml` | placeholder for the GenAI keys - **do not commit real values** |
| `03-services.yaml` | Deployment + Service for each of the five applications |
| `04-observability.yaml` | Zipkin (`tracing-server`) |
| `05-ingress.yaml` | exposes only the API Gateway |
| `06-mysql.yaml` | MySQL StatefulSet + PVC, its Secret, and the shared schema/seed ConfigMap |

## Deploy

```bash
# 1. Build and push the images to a registry your cluster can reach.
#    On a local cluster (kind, minikube, Docker Desktop) a local build is enough.
./mvnw clean install -PbuildDocker

# 2. Apply.
kubectl apply -f k8s/

# 3. Real keys, if you run genai-service.
kubectl create secret generic genai-secrets -n petclinic \
  --from-literal=OPENAI_API_KEY=sk-... \
  --dry-run=client -o yaml | kubectl apply -f -

# 4. Reach the UI without an ingress controller.
kubectl port-forward -n petclinic svc/api-gateway 8080:8080
```

## How discovery and configuration work here

Each pod mounts two ConfigMaps:

```
/config/common/application.yml   <- ConfigMap petclinic-common
/config/app/application.yml      <- ConfigMap <service>-config
```

The image sets `SPRING_CONFIG_ADDITIONAL_LOCATION` to both paths, so Spring Boot layers them over
the `application.yml` inside the jar, with `/config/app/` winning. Nothing fetches configuration
over the network at startup.

Services address each other by Service DNS name (`http://customers-service:8081`). The ClusterIP
Service load-balances across ready pods, which is why `replicas: 2` needs no further wiring and
why no `@LoadBalanced` client remains in the code.

## Trying it on minikube

Verified on minikube v1.39.0 / Kubernetes v1.37.0 with the docker driver.

```bash
minikube start --driver=docker --cpus=4 --memory=5000 --profile=petclinic

# Build, then push the images into the cluster's own image store.
# The manifests use imagePullPolicy: IfNotPresent, so no registry is needed.
./mvnw clean install -PbuildDocker
for img in customers-service vets-service visits-service api-gateway genai-service; do
  minikube image load --profile=petclinic "springcommunity/spring-petclinic-$img:latest"
done

kubectl apply -f k8s/
kubectl get pods -n petclinic -w

kubectl port-forward -n petclinic svc/api-gateway 8080:8080
# http://localhost:8080
```

`genai-service` will CrashLoopBackOff until the Secret holds a real key - it fails at startup on
`OpenAI API key must be set`. Either give it one, or `kubectl scale deploy/genai-service -n petclinic --replicas=0`.

Tear down with `minikube delete --profile=petclinic`.

### Checking that discovery really is the platform's job

The Service, not the application, is what spreads traffic. With `replicas: 2`, 40 requests sent to
`http://customers-service:8081` from inside the cluster land on both pods:

```bash
kubectl run lbtest -n petclinic --image=curlimages/curl:latest --restart=Never --command -- sleep 600
kubectl exec -n petclinic lbtest -- sh -c '
  for i in $(seq 1 40); do
    curl -s http://customers-service:8081/actuator/prometheus |
      awk "/^process_start_time_seconds/ {print \$2}"
  done' | sort | uniq -c
```

Two distinct start times means two pods served the traffic, with no client-side load balancer
anywhere in the gateway.

A rolling restart under continuous load should not drop a request, because the Service removes a
pod from its endpoints as soon as the readinessProbe fails, and `server.shutdown: graceful` lets
in-flight requests finish:

```bash
kubectl rollout restart deploy/customers-service -n petclinic
```

## Keeping ConfigMaps in sync with `../config`

`01-configmaps.yaml` duplicates the files under `../config` so the directory is self-contained.
After editing those files, regenerate rather than hand-editing:

```bash
kubectl create configmap petclinic-common -n petclinic \
  --from-file=application.yml=config/common/application.yml \
  --dry-run=client -o yaml | kubectl apply -f -

for svc in api-gateway customers-service vets-service visits-service genai-service; do
  kubectl create configmap "${svc}-config" -n petclinic \
    --from-file=application.yml="config/${svc}/application.yml" \
    --dry-run=client -o yaml | kubectl apply -f -
done
```

A ConfigMap change does not restart pods. Roll them:

```bash
kubectl rollout restart deployment -n petclinic
```

## The database

`customers-service`, `vets-service` and `visits-service` share one MySQL instance. This is not
optional decoration: with the default in-memory HSQLDB each pod gets its own private database
(`jdbc:hsqldb:mem:<random-uuid>`), so at `replicas: 2` a write lands on one pod and the other never
sees it. HSQLDB is only safe at `replicas: 1`.

The schema spans the three services - `visits.pet_id` is a foreign key onto `pets.id` - so it is
created once, by the MySQL container, from the `mysql-initdb` ConfigMap mounted into
`/docker-entrypoint-initdb.d`. The application pods run with `spring.sql.init.mode=never`, so they
neither race to create tables nor re-seed demo rows over real data on restart.

The ConfigMap mirrors the `db/mysql/*.sql` files in the three modules. Regenerate it after editing
them:

```bash
python scripts/generate_mysql_initdb.py
kubectl apply -f k8s/06-mysql.yaml
```

Those scripts only run when MySQL initialises an empty data directory, so an existing PVC keeps its
schema. To start clean:

```bash
kubectl delete statefulset petclinic-mysql -n petclinic
kubectl delete pvc data-petclinic-mysql-0 -n petclinic
kubectl apply -f k8s/06-mysql.yaml
```

Checking that the replicas really do share one dataset:

```bash
for ip in $(kubectl get pods -n petclinic -l app=customers-service -o jsonpath='{.items[*].status.podIP}'); do
  echo -n "$ip: "
  kubectl exec -n petclinic deploy/api-gateway -- curl -s "http://$ip:8081/owners" | grep -o '"id"' | wc -l
done
```

Both pods must report the same count, before and after a write.

## Probes

`application.yml` enables `management.endpoint.health.probes`, so each pod exposes:

* `/actuator/health/liveness` - used by `startupProbe` and `livenessProbe`
* `/actuator/health/readiness` - used by `readinessProbe`, and by the docker-compose healthchecks

`startupProbe` gives the JVM up to 150s to come up, which keeps the liveness and readiness periods
short without killing a cold-starting pod.

## Not included

* **A replicated database.** MySQL runs as a single-replica StatefulSet with a PVC. That is enough
  for the application replicas to share one dataset, but MySQL itself is a single point of failure -
  use a managed database or an operator for anything real.
* **Prometheus and Grafana.** The compose stack runs them directly; on a cluster use kube-prometheus-stack
  and let it scrape `/actuator/prometheus`, which every service already exposes.
* **NetworkPolicies, PodDisruptionBudgets, HPAs.** Worth adding for anything beyond a demo.
