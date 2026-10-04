#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

usage() {
  echo "Usage: $0 [--chaos-monkey]"
  echo "  --chaos-monkey  Start all apps with the chaos-monkey Spring profile enabled (default: disabled)"
}

CHAOS_MONKEY="no"
for arg in "$@"; do
  case "${arg}" in
    --chaos-monkey) CHAOS_MONKEY="yes" ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "Unknown option: ${arg}" >&2
      usage
      exit 1
      ;;
  esac
done

INFRA_SERVICES=(grafana-server prometheus-server tracing-server)

APP_MODULES=(config-server discovery-server customers-service visits-service vets-service genai-service api-gateway admin-server)

# Resolves the executable jar of a module (ignores sources/javadoc/original jars).
find_jar() {
  ls "$1"/target/*.jar 2>/dev/null | grep -v -- '-sources\|-javadoc\|\.original' | head -n 1 || true
}

# Starts a Spring Boot jar in the background and records its PID in target/<name>.pid.
start_app() {
  local module="$1" port="$2" name="$3" jar
  jar=$(find_jar "${module}")
  nohup java -jar "${jar}" --server.port="${port}" ${PROFILE_ARG} > "target/${name}.log" 2>&1 &
  echo $! > "target/${name}.pid"
}

# Polls a service actuator health endpoint until it answers.
# Fails fast if the process died, and only warns after a timeout.
wait_for_service() {
  local name="$1" port="$2" timeout="${3:-120}" waited=0
  echo "Waiting for ${name} on port ${port} (timeout: ${timeout}s)"
  until curl --silent --fail --output /dev/null "http://localhost:${port}/actuator/health"; do
    if ! kill -0 "$(cat "target/${name}.pid")" 2>/dev/null; then
      echo "Error: ${name} exited unexpectedly, last log lines (target/${name}.log):" >&2
      tail -n 20 "target/${name}.log" >&2
      exit 1
    fi
    if (( waited >= timeout )); then
      echo "Warning: ${name} did not become healthy within ${timeout}s (see target/${name}.log)" >&2
      return 0
    fi
    sleep 2
    waited=$((waited + 2))
  done
}

# Fail before touching anything if the project has not been built
for module in "${APP_MODULES[@]}"; do
  if [[ -z "$(find_jar "spring-petclinic-${module}")" ]]; then
    echo "Error: no jar found in spring-petclinic-${module}/target. Build the project first: ./mvnw clean install -DskipTests" >&2
    exit 1
  fi
done

pkill -9 -f spring-petclinic || echo "Failed to kill any apps"

# Containers have fixed names (container_name in docker-compose.yml): remove them, including
# stopped ones or ones created from another compose project/directory, to avoid name conflicts.
docker compose down --remove-orphans || echo "No docker containers are running"
docker rm --force "${INFRA_SERVICES[@]}" > /dev/null 2>&1 || true

PROFILE_ARG=""
if [[ "${CHAOS_MONKEY}" == "yes" ]]; then
  echo "Chaos Monkey activé (profil chaos-monkey)"
  PROFILE_ARG="--spring.profiles.active=chaos-monkey"
else
  echo "Chaos Monkey désactivé"
fi

echo "Running infra"
docker compose up -d "${INFRA_SERVICES[@]}"

echo "Running apps"
mkdir -p target
start_app spring-petclinic-config-server 8888 config-server
wait_for_service config-server 8888
start_app spring-petclinic-discovery-server 8761 discovery-server
wait_for_service discovery-server 8761
start_app spring-petclinic-customers-service 8081 customers-service
start_app spring-petclinic-visits-service 8082 visits-service
start_app spring-petclinic-vets-service 8083 vets-service
start_app spring-petclinic-genai-service 8084 genai-service
start_app spring-petclinic-api-gateway 8080 api-gateway
start_app spring-petclinic-admin-server 9090 admin-server
for service in "customers-service 8081" "visits-service 8082" "vets-service 8083" "genai-service 8084" "api-gateway 8080" "admin-server 9090"; do
  # shellcheck disable=SC2086
  wait_for_service ${service}
done
echo "All apps started"
