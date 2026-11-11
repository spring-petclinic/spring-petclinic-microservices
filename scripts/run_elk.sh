#!/usr/bin/env bash
# Starts the centralized logging ELK stack (Elasticsearch, Logstash, Kibana, Filebeat)
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR/.."

echo "Starting ELK stack..."
docker compose -f docker-compose-elk.yml up -d

echo "Waiting for Elasticsearch to become healthy..."
until curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green"\|"status":"yellow"'; do
  echo "Elasticsearch is initializing, waiting 5 seconds..."
  sleep 5
done

echo "ELK stack is up and running!"
echo "Elasticsearch: http://localhost:9200"
echo "Kibana:        http://localhost:5601"
echo "Logstash Beat: localhost:5044"
echo "Logstash TCP:  localhost:5000"

