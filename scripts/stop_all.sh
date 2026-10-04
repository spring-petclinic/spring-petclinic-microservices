#!/usr/bin/env bash
# Stops all services previously started by run_all.sh:
# - kills local java processes for spring-petclinic-*
# - removes the docker-compose infra containers (grafana, prometheus, tracing)

set -o errtrace
set -o nounset
set -o pipefail

echo "Stopping spring-petclinic java apps"
pkill -f spring-petclinic || echo "No spring-petclinic apps were running"

echo "Stopping docker infra containers"
docker compose down --remove-orphans || echo "No docker containers are running"
docker rm --force grafana-server prometheus-server tracing-server > /dev/null 2>&1 || true

echo "Done"
