#!/usr/bin/env bash
# Stops all services previously started by run_all.sh:
# - kills local java processes for spring-petclinic-*
# - stops the docker-compose infra containers (grafana, prometheus, tracing)

set -o errtrace
set -o nounset
set -o pipefail

echo "Stopping spring-petclinic java apps"
pkill -f spring-petclinic || echo "No spring-petclinic apps were running"

echo "Stopping docker infra containers"
docker compose kill || echo "No docker containers are running"

echo "Done"
