# Architecture

Two views: the system as it is at the baseline, and the target at the end of Phase 3.
GitHub renders the diagrams below directly.

## Today: baseline `aefaf7f`

Every request is synchronous. The gateway forwards it to one service and waits for the answer.
No other service learns that a visit was booked.

```mermaid
flowchart LR
    browser["Browser<br/>AngularJS UI"]

    subgraph platform["Platform"]
        config["config-server<br/>:8888"]
        discovery["discovery-server<br/>Eureka :8761"]
        admin["admin-server<br/>:9090"]
    end

    gateway["api-gateway<br/>:8080<br/>one shared fallback"]

    subgraph services["Business services"]
        customers["customers-service<br/>:8081"]
        visits["visits-service<br/>:8082"]
        vets["vets-service<br/>:8083"]
        genai["genai-service<br/>:8084"]
    end

    subgraph observability["Observability"]
        zipkin["Zipkin :9411"]
        prometheus["Prometheus :9091"]
        grafana["Grafana :3030"]
    end

    browser -->|HTTP| gateway
    gateway -->|"/api/customer/**"| customers
    gateway -->|"/api/visit/**"| visits
    gateway -->|"/api/vet/**"| vets
    gateway -->|"/api/genai/**"| genai

    services -.->|register, read config| platform
    gateway -.->|look up services| discovery
    services -.->|traces, metrics| observability
```

Each business service keeps its own data in an in-memory HSQLDB database (MySQL is optional).

## Target: end of Phase 3

Booking stays synchronous and returns as before. After the commit, visits-service publishes an
event; everything to the right of Kafka happens after the user already has their answer.

```mermaid
flowchart LR
    browser["Browser<br/>AngularJS UI<br/>+ bell, toast, banner (P3)"]

    gateway["api-gateway :8080<br/>+ rate limit (P3)<br/>+ visits circuit breaker<br/>and fallback (P3)"]
    redis[("Redis :6379<br/>rate-limit buckets (P3)")]

    subgraph services["Business services"]
        customers["customers-service"]
        visits["visits-service<br/>+ visit type, status,<br/>cancel, publisher (P2)"]
        vets["vets-service"]
        genai["genai-service"]
    end

    subgraph kafka["Kafka, KRaft mode (P1)"]
        topic[["visit-events<br/>3 partitions, key = petId"]]
        dlt[["visit-events.DLT"]]
    end

    alert["notification-alert-service (P2)<br/>port 8085, proposed<br/>skip duplicates, classify,<br/>store, push"]
    postgres[("PostgreSQL :5432<br/>audit_record (P2)")]
    kafkaui["Kafka UI :8090 (P1)"]

    platform["Platform, unchanged<br/>config-server, discovery-server,<br/>admin-server"]
    observability["Observability, unchanged<br/>Zipkin, Prometheus, Grafana"]

    browser -->|HTTP| gateway
    gateway --> customers
    gateway -->|"book / cancel"| visits
    gateway --> vets
    gateway --> genai
    gateway -->|"/api/alert/**"| alert
    gateway <--> redis

    visits ==>|"VisitEvent,<br/>after commit"| topic
    topic ==>|"group notification-group"| alert
    alert -->|"after 3 failed retries"| dlt
    alert -->|insert only| postgres
    alert ==>|"WebSocket /ws<br/>/topic/notifications"| gateway
    gateway ==>|live alert| browser

    kafkaui -.-> kafka
    services -.->|register, read config| platform
    alert -.->|register, read config| platform
    services -.->|traces, metrics| observability
    alert -.->|traces, metrics| observability
```

Thick arrows are the asynchronous path added by this project. `(P1)`, `(P2)` and `(P3)` mark
the phase that adds each part. The platform services and the observability stack stay as they
are today; they are drawn as one box each to keep the diagram readable. Port 8085 for
notification-alert-service is the plan's proposal and is confirmed in W7.

### How a booking flows

```mermaid
sequenceDiagram
    actor staff as Front-desk staff
    participant gw as api-gateway
    participant vs as visits-service
    participant k as Kafka visit-events
    participant na as notification-alert-service
    participant db as PostgreSQL
    participant ui as Open browsers

    staff->>gw: POST /api/visit/owners/{ownerId}/pets/{petId}/visits
    gw->>vs: forward
    vs->>vs: save visit, commit
    vs-->>gw: 201 Created
    gw-->>staff: 201 Created
    Note over vs,k: After the commit. If Kafka is down the booking<br/>still succeeds and the error is logged.
    vs-)k: VisitEvent, key = petId
    k-)na: deliver
    na->>na: skip if eventId already processed
    na->>na: classify EMERGENCY or ROUTINE
    na->>db: insert audit record
    na-)ui: push alert to /topic/notifications
    Note over k,na: After 3 failed retries the event<br/>goes to visit-events.DLT.
```

## Related documents

- [Spike: what the baseline lacks](spikes/baseline-limitations.md)
- [VisitEvent contract](../contracts/README.md)
- [Setup guide](../SETUP.md)
