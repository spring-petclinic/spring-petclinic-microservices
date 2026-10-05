# Phase 1 progress report and retrospective

> **How to use this file.** It is a template with the facts from the repository already filled
> in. Everything marked `TODO` must be completed by the team; do not submit with any `TODO` left.
> For submission, export to PDF in Arial 12.

| | |
|---|---|
| Project | Event-Driven Alert and Audit Pipeline for Spring PetClinic Microservices |
| Repository | https://github.com/JawadCEO/spring-petclinic-microservices |
| Sprint | 1 of 3 (Phase 1: setup and contracts) |
| Sprint dates | TODO start date to TODO end date |
| Team | Jawad (architecture and code), Rahul (testing), Sneha (infrastructure and documentation) |
| Scrum Master this sprint | TODO |
| Report date | TODO |

## 1. Sprint goal

The full stack plus Kafka runs on every laptop, and the team agrees on the event contract.
Substantial story: US9 (one compose file that starts everything).

## 2. Product backlog

| Item | Story | Points | Phase | Status |
|---|---|---|---|---|
| W1 | US9 part 1: one compose file with Kafka, smoke test, CI | 5 | 1 | Done |
| W2 | Enabler: VisitEvent contract | 3 | 1 | Done |
| W3 | Spikes, documentation, GitHub setup | 2 | 1 | TODO Done / In review |
| W4 | US1: publish a VisitEvent on booking | 5 | 2 | To do |
| W5 | US1: cancel a visit | 3 | 2 | To do |
| W6 | US2: visit type and classification | 5 | 2 | To do |
| W7 | US3: alert service and audit record | 8 | 2 | To do |
| W8 | US4: WebSocket push | 5 | 2 | To do |
| W9 | Enabler: run our own images | 2 | 2 | To do |
| W10 | US5: notification bell and toast | 5 | 3 | To do |
| W11 | US6: gateway rate limiting | 5 | 3 | To do |
| W12 | US7: visit-specific fallback | 5 | 3 | To do |
| W13 | US8: degraded banner | 3 | 3 | To do |
| W14 | US9 final: end-to-end run and guide | 5 | 3 | To do |
| | **Total** | **61** | | |

## 3. Sprint backlog: planned vs done

| Item | Planned | Done | Points planned | Points done | Evidence |
|---|---|---|---|---|---|
| W1 | Kafka (KRaft), kafka-init, Kafka UI in compose; smoke test; CI workflow; Windows notes in SETUP.md | Yes | 5 | 5 | [PR #1](https://github.com/JawadCEO/spring-petclinic-microservices/pull/1), merged |
| W2 | JSON Schema, examples, contract README, validate-events.py, contract test in CI | Yes | 3 | 3 | [PR #2](https://github.com/JawadCEO/spring-petclinic-microservices/pull/2), merged |
| W3 | Spike write-up, architecture diagrams, issue script, branch protection, this report | TODO | 2 | TODO | TODO PR link |
| | **Total** | | **10** | **TODO** | |

Velocity this sprint: TODO points.

### Changes from the plan

| Planned | Actual | Why |
|---|---|---|
| Contract test 11/11 | 16/16 (4 valid, 12 invalid examples) | Review of PR #2 asked for examples covering rules that had none: unknown field, empty description, impossible date, lower-case visit type, visit id 0 |
| Smoke test checks "containers, routes, topics" | Also a Kafka produce/consume round trip, on a separate `smoke-test` topic | Review of PR #1; a separate topic keeps test messages off `visit-events` |
| customers-service on host port 8081 | Port is configurable through `CUSTOMERS_PORT` in `.env`, default 8081 | Port 8081 was not available on at least one Windows laptop |
| Kafka UI image `provectuslabs/kafka-ui` | `kafbat/kafka-ui` | The original image is no longer maintained |
| SETUP.md as part of W3 | First version delivered in W1, extended in W3 | Review of PR #1 asked for it there |
| Baseline `aefaf7f` "matches the running Docker images" | The published images are Spring Boot 3.4.1; the baseline source is 4.0.1 | Found in the spike (finding 6). The running stack is not our code until W9 builds our own images |

### Contribution per member

| Member | Role | Work this sprint | Points | Commits / PRs / reviews |
|---|---|---|---|---|
| Jawad | Architecture and code | TODO | TODO | TODO |
| Rahul | Testing | TODO | TODO | TODO |
| Sneha | Infrastructure and documentation | TODO | TODO | TODO |

## 4. Gate 1 sign-off

Gate 1: all pull requests merged after review; smoke test and contract test pass on every
laptop; retrospective held; report and demo done.

### Smoke test (target 18/18)

Command: `./scripts/phase1-smoke-test.sh`

| Laptop | OS and shell | Date | Result | Evidence |
|---|---|---|---|---|
| Jawad | Windows 11, Git Bash | 2026-10-01 | 18/18 | TODO screenshot |
| Rahul | TODO | TODO | TODO /18 | TODO screenshot |
| Sneha | TODO | TODO | TODO /18 | TODO screenshot |

These results are for the published upstream images (Spring Boot 3.4.1 on Jawad's laptop) plus
our Kafka containers. They show that the stack and Kafka run together; they do not test a
build of the baseline source.

### Contract test (plan target 11/11, now 16/16)

Command: `python contracts/validate-events.py`

| Where | Environment | Date | Result | Evidence |
|---|---|---|---|---|
| CI | GitHub Actions, Python 3.12 | 2026-10-02 | 16/16 | "VisitEvent contract test" check on PR #2 |
| Jawad | Windows 11, Python 3.11 | 2026-10-01 | 16/16 | TODO screenshot |
| Rahul | TODO | TODO | TODO /16 | TODO screenshot |
| Sneha | TODO | TODO | TODO /16 | TODO screenshot |

### CI

| Check | What it does | Status on `main` |
|---|---|---|
| Validate docker-compose.yml | Compose syntax, Kafka services present | TODO |
| ShellCheck | Lints all shell scripts | TODO |
| VisitEvent contract test | Runs the 16 contract checks | TODO |
| Java CI with Maven | Builds and tests all modules | TODO |

### Gate checklist

- [ ] W1, W2 and W3 pull requests reviewed by another member and merged
- [ ] Smoke test 18/18 on all three laptops
- [ ] Contract test 16/16 on all three laptops and in CI
- [ ] `main` protected: pull request with one approval and passing checks required
- [ ] Phase 2 issues, milestone and project board created
- [ ] Retrospective held
- [ ] 5-minute demo given
- [ ] This report submitted

| Signed off by | Date |
|---|---|
| Jawad | TODO |
| Rahul | TODO |
| Sneha | TODO |

### W3 items that are GitHub settings, not files

These are part of W3 in the plan and cannot be delivered through a pull request. W3 is not
done until all three are ticked.

- [ ] Phase 2 issues and milestone created (`./scripts/create-github-issues.sh`; needs Issues
      enabled on the fork and the GitHub CLI)
- [ ] Project board created with the W4 to W9 issues on it
- [ ] `main` protected: pull request, one approval and passing checks required

## 5. Demo

Five minutes in total. Each member presents their own story and shows what a user or a new
contributor gains, not the code.

| Order | Presenter | Story | What is shown | Minutes |
|---|---|---|---|---|
| 1 | TODO | US9 part 1 (W1): one compose file with Kafka | TODO, for example `docker compose up -d`, Kafka UI with the topics, smoke test 18/18 | TODO |
| 2 | TODO | Enabler (W2): VisitEvent contract | TODO, for example the schema, one valid and one invalid example, contract test 16/16 | TODO |
| 3 | TODO | W3: spike findings and architecture | TODO, for example the fallback experiment and the target diagram | TODO |

Demo date: TODO. Questions or feedback received: TODO.

## 6. Scrum meeting log

| Date | Type | Attendees | Done since last meeting | Planned next | Blockers |
|---|---|---|---|---|---|
| TODO | Sprint planning | TODO | n/a | TODO | TODO |
| TODO | Daily scrum | TODO | TODO | TODO | TODO |
| TODO | Daily scrum | TODO | TODO | TODO | TODO |
| TODO | Sprint review | TODO | TODO | TODO | TODO |
| TODO | Retrospective | TODO | TODO | TODO | TODO |

## 7. Retrospective

Held on TODO date. Photo: TODO insert the team photo here.

| What went well | What did not go well | What we will change |
|---|---|---|
| TODO | TODO | TODO |
| TODO | TODO | TODO |
| TODO | TODO | TODO |

Action items for the next sprint:

| Action | Owner | By when |
|---|---|---|
| TODO | TODO | TODO |
| TODO | TODO | TODO |

Facts from this sprint the team may want to discuss (delete what does not apply):

- The first CI run on PR #1 passed, the second failed on a shellcheck false positive that only
  showed with the older shellcheck on the GitHub runner.
- genai-service did not start without an OpenAI key until a placeholder default was added.
- A stale Grafana container with the wrong port mapping made one smoke check fail locally.
- Both pull requests went through one round of requested changes before approval.

## 8. Architecture

Current and target diagrams, and the booking flow: [docs/architecture.md](../architecture.md).

What changed in the architecture this sprint: Kafka in KRaft mode, the `kafka-init` topic
setup and Kafka UI were added to the compose stack. No service produces or consumes events yet.

Findings about the baseline that shape the next sprints:
[docs/spikes/baseline-limitations.md](../spikes/baseline-limitations.md).

## 9. Next sprint (Phase 2: event pipeline and live push, 28 points)

Goal: booking a visit produces a classified, audited alert that appears live in every open browser.

| Item | Story | Points | Lead | Substantial story for |
|---|---|---|---|---|
| W4 | US1: publish a VisitEvent on booking | 5 | Jawad | TODO |
| W5 | US1: cancel a visit | 3 | Jawad | |
| W6 | US2: visit type and classification | 5 | Jawad | |
| W7 | US3: alert service and audit record | 8 | Jawad | TODO |
| W8 | US4: WebSocket push | 5 | Jawad | TODO |
| W9 | Enabler: run our own images | 2 | Sneha | |

Order: W4 and W6 first, then W7, then W8. W5 and W9 run alongside.

Flow diagram: the sequence diagram "How a booking flows" in [docs/architecture.md](../architecture.md).

Proposed methods per story. **This is a proposal, not a decided design**: the names are a
starting point for sprint planning and nothing in the code depends on them yet.

| Story | Proposed class | Proposed methods |
|---|---|---|
| US1 (W4) | `VisitResource` | `create(visit, ownerId, petId)` |
| US1 (W4) | `VisitEventPublisher` | `publishScheduled(visit, ownerId)` |
| US1 (W5) | `VisitResource` | `cancel(ownerId, petId, visitId)` |
| US2 (W6) | `AlertClassifier` | `classify(visitType)` |
| US3 (W7) | `VisitEventListener` | `onVisitEvent(event)` |
| US3 (W7) | `AuditResource` | `search(petId, ownerId, type, from, to)` |
| US4 (W8) | `AlertPushService` | `push(alert)` |

TODO: the team confirms or corrects these names during sprint planning, then removes the
word "proposed".

## 10. Next meeting

| | |
|---|---|
| Date and time | TODO |
| Place or link | TODO |
| Scrum Master for Sprint 2 | TODO |
| Agenda | Sprint 2 planning: confirm W4 to W9, assign substantial stories, answer the open questions from the spike |

## 11. Teammate evaluations

Each member rates the other two from 1 (low) to 5 (high) and adds one sentence.

| Evaluator | Teammate | Contribution | Reliability | Communication | Comment |
|---|---|---|---|---|---|
| Jawad | Rahul | TODO | TODO | TODO | TODO |
| Jawad | Sneha | TODO | TODO | TODO | TODO |
| Rahul | Jawad | TODO | TODO | TODO | TODO |
| Rahul | Sneha | TODO | TODO | TODO | TODO |
| Sneha | Jawad | TODO | TODO | TODO | TODO |
| Sneha | Rahul | TODO | TODO | TODO | TODO |
