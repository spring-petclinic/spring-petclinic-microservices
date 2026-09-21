#!/usr/bin/env bash
#
# Runs every service as a plain Spring Boot jar on the host.
#
# There is no config server and no service registry to wait for, so the apps
# all start at once: each one reads ./config/common, its own ./config/<name>
# and ./config/local (which points the peers at localhost instead of at
# container/Service names).

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

pkill -9 -f spring-petclinic || echo "Failed to kill any apps"

docker compose kill || echo "No docker containers are running"

PROFILE_ARG=""
if [[ "${CHAOS_MONKEY}" == "yes" ]]; then
  echo "Chaos Monkey enabled (chaos-monkey profile)"
  PROFILE_ARG="--spring.profiles.active=chaos-monkey"
else
  echo "Chaos Monkey disabled"
fi

echo "Running infra"
docker compose up -d grafana-server prometheus-server tracing-server

echo "Running apps"
mkdir -p target

start() {
  local name="$1" port="$2"
  local locations="optional:file:./config/common/,optional:file:./config/${name}/,optional:file:./config/local/"
  nohup java -jar spring-petclinic-"${name}"/target/*.jar \
    --server.port="${port}" \
    --spring.config.additional-location="${locations}" \
    ${PROFILE_ARG} > "target/${name}.log" 2>&1 &
}

start customers-service 8081
start visits-service    8082
start vets-service      8083
start genai-service     8084
start api-gateway       8080

echo "Waiting for apps to start"
sleep 60
