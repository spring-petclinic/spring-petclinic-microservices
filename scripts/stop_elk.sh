#!/usr/bin/env bash
# Stops the centralized logging ELK stack
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR/.."

echo "Stopping ELK stack..."
docker compose -f docker-compose-elk.yml down --remove-orphans

echo "ELK stack stopped."

