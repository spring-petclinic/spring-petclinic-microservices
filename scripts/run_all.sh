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

pkill -9 -f spring-petclinic || echo "Failed to kill any apps"

docker compose kill || echo "No docker containers are running"

PROFILE_ARG=""
if [[ "${CHAOS_MONKEY}" == "yes" ]]; then
  echo "Chaos Monkey activé (profil chaos-monkey)"
  PROFILE_ARG="--spring.profiles.active=chaos-monkey"
else
  echo "Chaos Monkey désactivé"
fi

echo "Running infra"
docker compose up -d grafana-server prometheus-server tracing-server

echo "Running apps"
mkdir -p target
nohup java -jar spring-petclinic-config-server/target/*.jar --server.port=8888 ${PROFILE_ARG} > target/config-server.log 2>&1 &
echo "Waiting for config server to start"
sleep 20
nohup java -jar spring-petclinic-discovery-server/target/*.jar --server.port=8761 ${PROFILE_ARG} > target/discovery-server.log 2>&1 &
echo "Waiting for discovery server to start"
sleep 20
nohup java -jar spring-petclinic-customers-service/target/*.jar --server.port=8081 ${PROFILE_ARG} > target/customers-service.log 2>&1 &
nohup java -jar spring-petclinic-visits-service/target/*.jar --server.port=8082 ${PROFILE_ARG} > target/visits-service.log 2>&1 &
nohup java -jar spring-petclinic-vets-service/target/*.jar --server.port=8083 ${PROFILE_ARG} > target/vets-service.log 2>&1 &
nohup java -jar spring-petclinic-genai-service/target/*.jar --server.port=8084 ${PROFILE_ARG} > target/genai-service.log 2>&1 &
nohup java -jar spring-petclinic-api-gateway/target/*.jar --server.port=8080 ${PROFILE_ARG} > target/gateway-service.log 2>&1 &
nohup java -jar spring-petclinic-admin-server/target/*.jar --server.port=9090 ${PROFILE_ARG} > target/admin-server.log 2>&1 &
echo "Waiting for apps to start"
sleep 60
