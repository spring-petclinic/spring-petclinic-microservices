# Phase 1 spike: what the baseline lacks

**Question.** What is missing in the upstream code for an event-driven alert and audit pipeline,
and what does that mean for the Phase 2 and 3 stories?

**Baseline.** Upstream `main` at commit `aefaf7f` (Spring Boot 4.0.1, Spring Cloud 2025.1.0,
Java 17).

**Method.** Reading the source at that commit, and for finding 4 an experiment on the running
stack (2 October 2026). The experiment ran on the published images, which are an older build
than the baseline source; see [finding 6](#6-the-published-images-are-not-the-baseline).

## Summary

| # | Finding | Checked by | Consequence | Work item |
|---|---|---|---|---|
| 1 | The visits table has no visit type and no status | Source | Cannot tell an emergency from a routine visit, cannot cancel | W5, W6 |
| 2 | Booking a visit drops the owner id | Source | The event and the audit record would have no owner | W4 |
| 3 | No messaging, rate limiting or audit trail | Source | Everything has to be added; nothing to extend | W4, W7, W8, W11 |
| 4 | One shared gateway fallback, written for the chat | Experiment | Visit failures get a chat message or a 405 | W12, W13 |
| 5 | The React client cannot connect; AngularJS is the working UI | Team report, not tested | UI work targets the AngularJS client unless decided otherwise | W10, W13 |
| 6 | The published Docker images are Spring Boot 3.4.1, not the 4.0.1 baseline | Container logs | Until W9, the running stack is not the code we change | W9 |

## 1. Visits have no type and no status

`visits` has four columns in both schemas: `id`, `pet_id`, `visit_date`, `description`
(`spring-petclinic-visits-service/src/main/resources/db/hsqldb/schema.sql` and
`db/mysql/schema.sql`). The `Visit` entity has the same four fields.

- There is no visit type, so nothing in the data says whether a visit is urgent.
- There is no status, and `VisitResource` has no cancel, update or delete endpoint.
  A visit, once booked, cannot be cancelled.
- The booking form (`scripts/visits/visits.controller.js`) sends only `date` and `description`.

**Consequence.** W6 adds `visit_type` (default `ROUTINE`) and W5 adds `status`
(default `SCHEDULED`) to both schemas, the entity and the form. Defaults keep existing rows
and existing callers valid.

## 2. Booking drops the owner id

The UI posts to `api/visit/owners/{ownerId}/pets/{petId}/visits`, so the owner id is in the URL.
The endpoint ignores it:

```java
@PostMapping("owners/*/pets/{petId}/visits")
public Visit create(@Valid @RequestBody Visit visit, @PathVariable("petId") @Min(1) int petId)
```

The owner segment is a wildcard. It is neither read nor stored, and it is not validated, so
any value is accepted.

**Consequence.** The VisitEvent contract requires `ownerId`. W4 changes the mapping to
`owners/{ownerId}/pets/{petId}/visits` and passes the owner id into the event. The URL the UI
calls stays the same. Whether `ownerId` is also stored in the `visits` table is still open (see
[Open questions](#open-questions)); it matters for W5, where the cancel event needs it too.

## 3. No messaging, rate limiting or audit trail

A search of every `pom.xml`, `application.yml` and Java source at the baseline finds no Kafka,
AMQP, Redis, WebSocket, rate limiter or audit code.

- **Messaging.** All calls are synchronous HTTP through the gateway. No service learns that a
  visit was booked unless it asks.
- **Live push.** The UI only shows new data after a reload.
- **Rate limiting.** The gateway has no `RequestRateLimiter` filter and no Redis.
- **Audit.** Nothing records who booked what and when; `visits` holds the current state only.

**Consequence.** These are new components, not extensions: Kafka (done in W1), the publisher
(W4), notification-alert-service with PostgreSQL (W7), WebSocket push (W8), Redis and rate
limiting (W11).

## 4. One shared fallback, written for the chat

The gateway applies a default circuit breaker to **every** route and forwards failures to a
single endpoint (`spring-petclinic-api-gateway/src/main/resources/application.yml`):

```yaml
default-filters:
  - name: CircuitBreaker
    args:
      name: defaultCircuitBreaker
      fallbackUri: forward:/fallback
```

`FallbackController` handles `/fallback` for POST only and always answers
`503 Chat is currently unavailable. Please try again later.`

**Experiment.** Tested build: `springcommunity/spring-petclinic-api-gateway:latest`, digest
`sha256:250c96946936…`, built 31 August 2025, Spring Boot 3.4.1. This is not a build of
`aefaf7f`. The baseline source has the same default filter and the same POST-only
`FallbackController`, so the same behaviour is expected there, but it has not been run on a
baseline build yet. It should be repeated once W9 produces our own images.

With the stack running, `docker compose stop visits-service`, then call the gateway:

| Request | Answer |
|---|---|
| `POST /api/visit/owners/1/pets/1/visits` (book a visit) | `503`, body `Chat is currently unavailable. Please try again later.` |
| `GET /api/visit/pets/visits?petId=1` (list visits) | `405 Method Not Allowed`, because the fallback only accepts POST |
| `GET /api/gateway/owners/1` (owner page) | `200`, with `"visits": []` for every pet |

So a failed booking is reported as a chat problem, a failed read is reported as a wrong HTTP
method, and the owner page silently shows no visits, which looks the same as a pet that has
never had one.

Two related observations:

- The same default filters retry a `POST` once on `SERVICE_UNAVAILABLE`. A booking is not
  idempotent, so a retry after a slow first attempt could store the visit twice. Each stored
  visit would publish its own event with its own `eventId`, so duplicate protection in the
  consumer would not catch it.
- The breaker name `defaultCircuitBreaker` is shared by all routes, so failures in one service
  count towards opening the breaker for the others.

**Consequence.** W12 gives the visits route its own circuit breaker and a visit-specific
fallback that works for GET and POST and carries a degraded marker. The chat fallback stays for
the genai route only. W13 shows a banner in the UI when it sees the marker.

## 5. The React client cannot connect

**Status: reported by the team, not tested, out of scope for this spike.**

The team tried a separate React client for PetClinic and could not get it to talk to this
backend. That client is not part of this repository. This write-up has no record of which
client and version was tried or what the error was, so the finding cannot be reproduced from
what is written here. Whoever ran the attempt should add those three facts below, or the team
should drop the finding.

| Client and version | What was tried | Error seen |
|---|---|---|
| TODO | TODO | TODO |

What is verified: the AngularJS client served by the gateway
(`spring-petclinic-api-gateway/src/main/resources/static`) works against the running stack;
the smoke test loads owners, vets and visits through the same gateway routes it uses.

**Consequence.** W10 (bell and toast) and W13 (banner) are planned against the AngularJS client.
The UI choice (Option A or B in the plan) is confirmed before Phase 3 starts.

## 6. The published images are not the baseline

`docker-compose.yml` runs `springcommunity/spring-petclinic-*:latest`. On the laptop used for
this spike those images were built on 31 August 2025 and their start-up logs show
**Spring Boot 3.4.1**. The baseline source at `aefaf7f` is **Spring Boot 4.0.1**
(`pom.xml`, `spring-boot-starter-parent`).

The project plan says the baseline was chosen partly because it "matches the running Docker
images". On this evidence it does not: the images match the older v3.4.1 release.

**Consequence.**

- The Phase 1 smoke test results (18/18) describe the 3.4.1 images plus our Kafka containers,
  not a build of the baseline source.
- Any behaviour observed on the running stack, including the experiment in finding 4, must be
  confirmed again on our own build.
- W9 (build and run our own images) is what closes this gap. It should come early in Phase 2,
  before the W4 to W8 demos rely on the running stack.
- Each member should check their own images, since `latest` can differ per laptop:
  `docker logs api-gateway 2>&1 | grep "Spring Boot"`.

## Open questions

The team has not fixed sprint dates yet, so "Decide by" names the meeting; replace it with the
date once the dates are confirmed.

| Question | Needed by | Decide by | Owner |
|---|---|---|---|
| Store `ownerId` in `visits`, or look it up when cancelling? | W4, W5 | Sprint 2 planning | Jawad |
| Remove the POST retry for the visits route, or make booking idempotent? | W4, W12 | Sprint 2 planning | Jawad |
| `petclinic.alerts.urgent-types`: fork of the config repository, or native profile? | W6 | Sprint 2 planning | Sneha |
| Move W9 (own images) to the start of Phase 2, given finding 6? | W4 to W8 | Sprint 2 planning | Sneha |
| Which UI gets the bell and banner (Option A or B)? | W10, W13 | Sprint 3 planning | Team |

## Related documents

- [Architecture diagrams](../architecture.md)
- [VisitEvent contract](../../contracts/README.md)
- [Setup guide](../../SETUP.md)
