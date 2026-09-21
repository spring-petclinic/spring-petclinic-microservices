# Running Spring PetClinic Microservices locally (Windows + Docker Desktop)

This stack is the Kubernetes-adapted version: **no config-server, no discovery-server, no admin-server**.
Services read their configuration from `./config` and address each other by container name, exactly as
they will address Kubernetes Services by DNS name.

Because the code differs from the published `springcommunity/*` images, **a Maven build is required** -
you can no longer just pull from Docker Hub.

Run everything from this folder in PowerShell:

```powershell
cd $HOME\dev\petclinic
```

## 0. Check the prerequisites

```powershell
docker version
docker compose version
java -version      # 17 or newer
```

If `docker version` errors, start Docker Desktop and wait for the whale icon to go steady.

## 1. Build the images

```powershell
.\mvnw clean install -PbuildDocker
```

This compiles all five services and produces the local `springcommunity/spring-petclinic-*` images.
First run downloads the Maven dependencies, so expect several minutes.

To skip the tests: `.\mvnw clean install -PbuildDocker -DskipTests`

## 2. Start the core stack

Skips `genai-service` (needs an OpenAI key) and Grafana/Prometheus:

```powershell
docker compose up -d petclinic-mysql customers-service vets-service visits-service api-gateway tracing-server
```

There is no startup ordering any more. Every container starts at once, just as pods do in a cluster.
The gateway returns its circuit-breaker fallback for the first 20-40 seconds while the JVMs warm up.

## 3. Watch it come up

```powershell
docker compose ps
docker compose logs -f api-gateway
```

Each container has a healthcheck against `/actuator/health/readiness` - the same endpoint the
Kubernetes readinessProbe uses. Wait until all say `healthy`.

## 4. Open the app

| URL | What |
|---|---|
| http://localhost:8080 | PetClinic UI (API gateway) |
| http://localhost:9411/zipkin/ | Zipkin traces |
| http://localhost:8081/owners | customers-service direct |
| http://localhost:8082/pets/visits?petId=7 | visits-service direct |
| http://localhost:8083/vets | vets-service direct |

Quick end-to-end check of the cross-service aggregation (gateway -> customers + visits):

```powershell
curl http://localhost:8080/api/gateway/owners/6
```

## 5. Where the configuration comes from

Each container mounts two directories:

```
./config/common     -> /config/common     (shared:   ConfigMap petclinic-common)
./config/<service>  -> /config/app        (per-app:  ConfigMap <service>-config)
```

The image sets `SPRING_CONFIG_ADDITIONAL_LOCATION` to both, layered over the `application.yml`
inside the jar. Precedence, lowest first:

1. `application.yml` in the jar - defaults
2. `/config/common/application.yml` - shared settings (Zipkin endpoint, log levels)
3. `/config/app/application.yml` - per-service settings (downstream URLs)

To see what a running service actually resolved, and from where:

```powershell
curl http://localhost:8080/actuator/env/petclinic.customers-service.url
```

Editing a file under `./config` needs only a restart of that container, not a rebuild:

```powershell
docker compose restart api-gateway
```

## 6. Optional extras

Grafana + Prometheus (these build locally, slower):

```powershell
docker compose up -d prometheus-server grafana-server
# Grafana    http://localhost:3030  (anonymous access, Prometheus datasource preconfigured)
# Prometheus http://localhost:9091
```

GenAI chatbot - only with a real key. Put it in `.env` first, then:

```powershell
docker compose up -d genai-service    # http://localhost:8084
```

## 7. Stop

```powershell
docker compose down          # stop and remove containers
docker compose down -v       # also drop volumes
```

---

## Resource notes

Each Java service is capped at 512 MB via `deploy.resources.limits.memory` in `docker-compose.yml`.

| Set | Containers | Approx. memory |
|---|---|---|
| Core (step 2) | 6 | ~3.0 GB |
| Everything | 9 | ~4.1 GB |

Three infrastructure services are gone; MySQL adds one back.
Give Docker Desktop at least 6 GB. On WSL2 that is set in `%USERPROFILE%\.wslconfig`:

```ini
[wsl2]
memory=8GB
processors=4
```

Then `wsl --shutdown` and restart Docker Desktop.

## Ports used

3306, 8080, 8081, 8082, 8083, 8084, 9091, 9411, 3030

If one is taken, `docker compose up` fails with a bind error. Change the left-hand side of the
`ports:` mapping in `docker-compose.yml`.

## Troubleshooting

**`docker compose up` says the image is not found** - you skipped step 1. The compose file uses
locally built images, not Docker Hub.

**Containers restart in a loop** - usually memory. Check `docker stats` and raise the Docker Desktop
memory limit.

**`genai-service` exits immediately** - no API key. It fails at startup on
`OpenAI API key must be set`. Leave it out, or put a real key in `.env`.

**Gateway returns 503 after two minutes** - check `docker compose ps` for a container that never went
`healthy`, then `docker compose logs <that-service>`. A 503 from `/api/genai/**` with the other routes
working is the circuit-breaker fallback, not a routing problem.

**A config change had no effect** - confirm what the app actually resolved via
`/actuator/env/<property>`; the response names the file it came from.

## Running the jars on the host instead of in containers

`./scripts/run_all.sh` starts Zipkin/Grafana/Prometheus in Docker and the five applications as plain
`java -jar` processes. It adds `config/local/` as a third config location, which repoints the peers
from container names to `localhost`. Logs land in `target/<service>.log`. Stop with
`./scripts/stop_all.sh`.

## The database

The stack runs **MySQL**, not the in-memory HSQLDB, and this matters for the Kubernetes work.

With HSQLDB the JDBC URL is `jdbc:hsqldb:mem:<random-uuid>` - a private database per JVM. One
container per service hides that, but the Kubernetes manifests run `replicas: 2`, where it means a
write lands on one pod and the other never sees it. MySQL is what makes replication correct.

The three data-owning services share one schema, because `visits.pet_id` is a foreign key onto
`pets.id`. So the schema is created **once by the MySQL container**, not by the applications: the six
`db/mysql/*.sql` files are bind-mounted into `/docker-entrypoint-initdb.d` with numeric prefixes that
fix the order. The services run with `spring.sql.init.mode=never`, so restarting one never re-seeds
demo rows over your data.

Inspect it directly:

```powershell
docker exec -it petclinic-mysql mysql -upetclinic -ppetclinic petclinic -e "SELECT COUNT(*) FROM owners;"
```

Data lives in the `mysql-data` volume and survives `docker compose down`. To start from a clean
database:

```powershell
docker compose down -v
```

Those init scripts only run when the data directory is empty, so **after editing any
`db/mysql/*.sql` you must `docker compose down -v`** for the change to take effect. For Kubernetes,
also regenerate the ConfigMap with `python scripts/generate_mysql_initdb.py`.

### Falling back to HSQLDB

Drop the `SPRING_PROFILES_ACTIVE`/`MYSQL_*` environment entries for a service in
`docker-compose.yml` and it reverts to in-memory HSQLDB. Fine for a quick single-instance run;
do not pair it with more than one replica.

## Next step: Kubernetes

The manifests are in [k8s/](k8s/) - see [k8s/README.md](k8s/README.md). The ConfigMaps there hold the
same content as `./config`, so what you verified here is what runs in the cluster.
