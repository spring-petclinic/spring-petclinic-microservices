# Phase 2 progress: event pipeline and live push

Goal: booking a visit produces a classified, audited alert that appears live in every open
browser. This file records what each work item changed and how it was tested.

## Status

The code for every item is written and tested on the laptop below. **Nothing is merged yet**:
each item is one branch stacked on the previous one, waiting for review.

| Order | Item | Story | Points | Plan lead | Branch |
|---|---|---|---|---|---|
| 1 | W4 | US1: publish a VisitEvent when a visit is booked | 5 | Jawad | `feature/w4-publish-visit-event` |
| 2 | W6 | US2: visit type and classification | 5 | Jawad | `feature/w6-visit-type` (classifier in W7) |
| 3 | W5 | US1: cancel a visit | 3 | Jawad | `feature/w5-cancel-visit` |
| 4 | W7 | US3: alert service with a permanent audit record | 8 | Jawad | `feature/w7-alert-service` |
| 5 | W8 | US4: push alerts over WebSocket | 5 | Jawad | `feature/w8-websocket-push` |
| 6 | W9 | Enabler: run our own images in Docker | 2 | Sneha | `feature/w9-own-images` |
| 7 | W15 | Enabler: regression tests for the UI and the backend | 3 | Sneha | `feature/w15-regression-tests` |

W9 was suggested first, but it was built after the services so that it could be proven end to end.

## Gate 2 check

Gate 2: a booking made in the UI creates exactly one audit row and a live WebSocket message; all
pull requests reviewed and merged; tests green in CI; demo and report done.

| Condition | State |
|---|---|
| Booking in the UI gives one audit row and one live message | **Seen working** on 2026-11-01, see below |
| Pull requests reviewed and merged | Not yet |
| Tests green in CI | Not yet: CI runs when the pull requests are opened |
| Demo and report | Not yet |

What was done in a real browser against the stack, through the gateway:

1. Subscribed to `/topic/notifications` over `ws://localhost:8080/ws` with raw STOMP frames.
2. Booked a visit with type EMERGENCY in the visit form.
3. One STOMP message arrived, severity EMERGENCY, and the audit search showed one new record.
4. Clicked Cancel on the owner page: status CANCELLED, one `VISIT_CANCELLED` message and record.
5. A second cancel returned 409.

## How a booking flows

1. `POST /owners/{ownerId}/pets/{petId}/visits` reaches `VisitResource`, which calls `VisitService`.
2. `VisitService` saves the visit and raises a `VisitEvent` inside the transaction.
3. After the commit, `VisitEventPublisher` sends it to `visit-events`, key = petId, on its own thread.
   The request has already been answered with 201.
4. `VisitEventListener` in notification-alert-service reads it in group `notification-group`.
5. `AlertService` skips an event id it already has, classifies, and inserts one `audit_record` row.
6. `AlertPushService` sends the alert to `/topic/notifications`.
7. An event that still fails after 3 retries goes to `visit-events.DLT`.

## What each item changed

**W4.** The booking endpoint maps the owner id instead of dropping it; an owner id below 1 returns
400. Adds `VisitService`, `VisitEvent`, `VisitEventPublisher`, and Kafka settings.

**W6.** `visit_type` column, default ROUTINE, upper case, up to 40 characters; dropdown in the form;
type on the visit and owner pages. `AlertClassifier` is in the alert service: EMERGENCY if the type
is in `petclinic.alerts.urgent-types`, otherwise ROUTINE. Also: `visit_date` is mapped as
`LocalDate`, because the timestamp mapping shifted the date by a day on a JVM not in UTC.

**W5.** `status` column, default SCHEDULED, not settable by a client.
`PUT owners/{ownerId}/pets/{petId}/visits/{visitId}/cancel` keeps the row and publishes
`VISIT_CANCELLED`; 409 if already cancelled, 404 if the pet has no such visit. Cancel button on the
owner page.

**W7.** New module `spring-petclinic-notification-alert-service`, port 8085, registered in Eureka,
at `/api/alert/**`. PostgreSQL table `audit_record` with a unique `event_id`; immutable entity; a
repository with no delete. `GET /audit` filters by `petId`, `ownerId`, `type`, `from`, `to`.

**W8.** STOMP endpoint `/ws`, topic `/topic/notifications`, gateway route `/ws/**`. Only a newly
stored record is pushed, so a redelivered event pushes nothing.

**W9.** Compose runs `petclinic/*` images built from this repository, so the stack is now Spring
Boot 4.0.1 rather than the published 3.4.1 (spike finding 6). Adds the alert service and PostgreSQL
with the volume `alerts-db`. `scripts/phase2-smoke-test.sh`.

**W15.** `scripts/regression-backend.sh` and [the UI checklist](../testing/ui-regression.md).

## Design decisions

- **After the commit, on its own thread.** No event for a rolled-back visit, and a slow or
  unreachable Kafka can neither fail nor delay a booking.
- **One sending thread.** Events leave in commit order. If Kafka is down, up to 1,000 wait; beyond
  that they are dropped and logged.
- **The owner id of a cancel event comes from the URL**, as for booking. It is not stored in
  `visits`, and not checked against the customers service, so a wrong owner id in the URL
  would be recorded as sent. This answers the spike's open question for now.
- **Retries cover every failure.** A database outage also sends events to the DLT after 3 retries,
  and nothing replays them yet.
- **Audit table created by the service itself.** The shared config server overrides
  `spring.sql.init.*` and `server.port` for every service. The first run on the real stack showed
  this: the table did not exist. The test profile now sends the same shared settings.

## Test results

All on Jawad's laptop (Windows 11, JDK 17, Docker Desktop), 2026-11-01.

| Test | Result |
|---|---|
| visits-service unit and integration tests | 22/22 |
| notification-alert-service tests, with real Kafka and PostgreSQL containers | 13/13 |
| api-gateway tests | 4/4 |
| `scripts/phase2-smoke-test.sh` | Phase 1 18/18 and Phase 2 9/9 |
| `scripts/regression-backend.sh` | 15/15 |
| UI checklist | 13/13 steps |

## Known limits

- **A failed send loses the event.** There is no retry or outbox on the producer side.
- **Nothing replays the dead-letter topic.** Events there stay until someone handles them.
- **The gateway retries a failed POST once.** That could store a visit twice, with two events and
  two audit rows. Still open; W12 is the natural place.
- **The MySQL schema** only gets the new columns on a new database (`CREATE TABLE IF NOT EXISTS`).
  An existing MySQL database needs them added by hand. MySQL was not run.
- **No login**, so `/ws` and the audit search are open. Login is out of scope in the plan.
- **The bell and toast are Phase 3** (W10), so staff do not see live alerts in the UI yet.
- **Rahul's and Sneha's laptops** have not run any of this.

## Open questions

| Question | Needed by | Owner |
|---|---|---|
| Remove the gateway's POST retry for the visits route, or make booking idempotent? | W12 | Jawad |
| What should happen to events in the dead-letter topic? | W14 | Team |
