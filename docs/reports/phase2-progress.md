# Phase 2 progress: event pipeline and live push

Goal: booking a visit produces a classified, audited alert that appears live in every open
browser. This file records what each work item changed, how it was tested and what is still
open. It is updated with every Phase 2 pull request.

## Status

| Item | Story | Points | Lead | Status |
|---|---|---|---|---|
| W4 | US1: publish a VisitEvent when a visit is booked | 5 | Jawad | In review |
| W5 | US1: cancel a visit | 3 | Jawad | To do |
| W6 | US2: visit type and alert classification | 5 | Jawad | To do |
| W7 | US3: alert service with a permanent audit record | 8 | Jawad | To do |
| W8 | US4: push alerts over WebSocket | 5 | Jawad | To do |
| W9 | Enabler: run our own images in Docker | 2 | Sneha | To do |

Suggested order: W9 first (until then Docker runs the published Spring Boot 3.4.1 images, not
our code), then W4 and W6, then W7, then W8; W5 alongside.

## W4: publish a VisitEvent when a visit is booked

**Story.** As a developer, I want visits-service to publish an event whenever a visit is
booked, so other services can react without a direct call.

### What changed

| File | Change |
|---|---|
| `web/VisitResource.java` | Booking maps `owners/{ownerId}/pets/{petId}/visits`; the owner id is no longer dropped. An owner id below 1 returns 400 |
| `service/VisitService.java` | New. Saves the visit in a transaction and announces it |
| `events/VisitEvent.java`, `VisitEventType.java` | New. The event, field for field as in `contracts/visit-event-schema.json` |
| `events/VisitEventPublisher.java` | New. Sends the event to `visit-events`, key = petId, as JSON |
| `events/EventsConfig.java` | New. The single thread that sends events |
| `src/main/resources/application.yml` | Kafka at `localhost:29092`, or `kafka:9092` in the docker profile; send timeouts |
| `pom.xml` | `spring-boot-starter-kafka`; test dependencies `spring-kafka-test` and `json-schema-validator` |
| `docker-compose.yml` | visits-service starts after kafka (start order only) |

All Java paths are under `spring-petclinic-visits-service/src/main/java/org/springframework/samples/petclinic/visits/`.

### How a booking flows

1. `POST /owners/{ownerId}/pets/{petId}/visits` reaches `VisitResource`, which calls `VisitService.schedule`.
2. `VisitService` saves the visit and raises a `VisitEvent` inside the transaction.
3. After the commit, `VisitEventPublisher` receives the event on the `visit-events-1` thread and sends it to Kafka.
4. The request thread has already returned `201` with the same body as before.

### Design decisions

- **After the commit, on its own thread.** An event is never sent for a visit that was rolled
  back, and Kafka being slow or down can neither fail nor delay the booking.
- **One sending thread.** Events leave in the order the visits were committed. If Kafka is down
  the queue holds up to 1,000 events; beyond that, events are dropped and logged.
- **A failed send is logged, not retried.** The visit is saved; the event is lost. An outbox
  would close this gap and is not part of the plan.
- **`visitType` is always `ROUTINE`** until W6 adds the visit type.
- **Own JSON mapper.** The wire format does not change if the application's JSON settings do.
- **Owner id below 1 returns 400.** Before W4 any value was accepted because it was ignored;
  the contract requires a real owner id. The UI always sends one.
- **Visit without a date.** The visit is saved but no event is sent, because the contract
  requires a date. A warning is logged.

### Tests

Run with `./mvnw -pl spring-petclinic-visits-service test`. 14 tests, all passing.

| Test class | What it checks |
|---|---|
| `VisitEventIntegrationTest` | Through the REST endpoint with an embedded Kafka broker: 201, and exactly one event on `visit-events` with key = petId and the owner id |
| `KafkaDownIntegrationTest` | Nothing listens on the Kafka address: the application starts, the booking is saved and answered with 201 |
| `VisitEventContractTest` | The published JSON validates against `contracts/visit-event-schema.json` with format checking on; the check rejects an impossible date, owner id 0 and an unknown event type |
| `VisitEventPublisherTest` | Topic, key and JSON payload; a send that throws or fails later does not propagate |
| `VisitServiceTest` | The visit is saved for the pet in the path; exactly one event with the owner id; no event without a date |
| `VisitResourceTest` | 201 with the same body as before; owner id passed on; owner id 0 rejected with 400; reading visits unchanged |

| Run | Result | By | Date |
|---|---|---|---|
| Visits-service tests, Windows 11, JDK 17 | 14/14 pass | Jawad | 2026-10-24 |
| Visits-service tests, CI | pending | GitHub Actions | |

### Not covered yet

- Not run against the Kafka container of the compose stack: Docker still runs the published
  visits-service image until W9. After W9, book a visit in the UI and check the event in
  Kafka UI (http://localhost:8090, topic `visit-events`).
- The gateway retries a failed booking POST once, which could store a visit twice and publish
  two events with different ids. Open question from the spike; to be settled in W4/W12 review.

## Open questions

| Question | Needed by | Owner |
|---|---|---|
| Store `ownerId` in `visits`, or look it up when cancelling? | W5 | Jawad |
| Remove the gateway's POST retry for the visits route, or make booking idempotent? | W12 | Jawad |
| `petclinic.alerts.urgent-types`: fork of the config repository, or native profile? | W6 | Sneha |
