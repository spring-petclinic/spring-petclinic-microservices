# Monitoring Setup for Spring Petclinic Microservices

This document explains how to use the monitoring tools set up for the Spring Petclinic Microservices application.

## Components

The monitoring stack consists of the following components:

1. **Prometheus** - For metrics collection and storage
2. **Grafana** - For visualization of metrics and logs
3. **Grafana Loki** - For log aggregation
4. **Promtail** - For log collection and forwarding to Loki
5. **Zipkin** - For distributed tracing

## Accessing the Monitoring Tools

After starting the application with Docker Compose, you can access the monitoring tools at the following URLs:

- **Grafana**: http://localhost:3030
  - Default username/password: admin/admin (Anonymous access is enabled)
  - Preconfigured dashboards are available for metrics and logs

- **Prometheus**: http://localhost:9091
  - Used for querying metrics directly

- **Zipkin**: http://localhost:9411
  - Used for viewing distributed traces

## Available Dashboards in Grafana

1. **Spring Petclinic Dashboard** - Shows metrics from the microservices
2. **Loki Logs Dashboard** - Shows logs from all services

## Metrics Available

The following metrics are collected by Prometheus:

- HTTP request counts and latencies
- JVM metrics (memory, garbage collection, etc.)
- System metrics (CPU, memory, etc.)

## Log Collection

Logs from all microservices are collected by Promtail and sent to Loki. You can view these logs in the Grafana Loki Logs Dashboard.

## Tracing

Distributed tracing is provided by Zipkin. You can view traces for requests that span multiple services.

## Starting the Monitoring Stack

The monitoring stack is included in the main `docker-compose.yml` file and will start automatically when you run:

```bash
docker-compose up -d
```

## Troubleshooting

If logs are not appearing in Grafana:

1. Make sure the log files exist in the correct location
2. Check that Promtail is running: `docker-compose ps promtail`
3. Check Promtail logs: `docker-compose logs promtail`