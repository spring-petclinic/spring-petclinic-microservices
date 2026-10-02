#!/usr/bin/env bash
# Creates the Phase 2 milestone, labels and issues (W4 to W9) with the GitHub CLI.
# Safe to run again: an existing milestone, label or issue is left alone.
#
# Usage (from anywhere):
#   ./scripts/create-github-issues.sh --dry-run     # show what would be created, needs no login
#   ./scripts/create-github-issues.sh               # create them
#
# Options:
#   --dry-run            print the plan, change nothing
#   --repo OWNER/NAME    target repository (default: JawadCEO/spring-petclinic-microservices)
#   --due YYYY-MM-DD     due date for the milestone (default: none)
#
# Each issue is assigned to its lead. The GitHub user names come from these environment
# variables; an empty one leaves that person's issues unassigned:
#   ASSIGNEE_JAWAD (default: JawadCEO)   ASSIGNEE_SNEHA (default: SnehaVarra3436)
#
# Before the real run:
#   - Install the GitHub CLI (https://cli.github.com) and sign in with: gh auth login
#   - Enable issues on the fork: Settings > General > Features > Issues. Forks have them off.
#
# The repository is always passed explicitly. In a fork, gh would otherwise pick the upstream
# project as its default and create the issues there.

set -o errexit
set -o nounset
set -o pipefail

ASSIGNEE_JAWAD="${ASSIGNEE_JAWAD-JawadCEO}"
ASSIGNEE_SNEHA="${ASSIGNEE_SNEHA-SnehaVarra3436}"

REPO="JawadCEO/spring-petclinic-microservices"
MILESTONE="Phase 2: Event pipeline and live push"
MILESTONE_DESCRIPTION="Booking a visit produces a classified, audited alert that appears live in every open browser. 28 points, W4 to W9."
DUE=""
DRY_RUN="no"

usage() {
  sed -n '2,23p' "$0" | sed 's/^# \{0,1\}//'
}

# valid_date <YYYY-MM-DD>: shape and ranges only, enough to catch typos before calling the API
valid_date() {
  [[ "$1" =~ ^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$ ]]
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN="yes" ;;
    --repo) REPO="${2:?--repo needs OWNER/NAME}"; shift ;;
    --due) DUE="${2:?--due needs YYYY-MM-DD}"; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
  shift
done

if [[ -n "${DUE}" ]] && ! valid_date "${DUE}"; then
  echo "ERROR: --due must be a date like 2026-10-22, got '${DUE}'" >&2
  exit 1
fi

CREATED=0
SKIPPED=0
EXISTING_TITLES=""
EXISTING_LABELS=""

say() {
  if [[ "${DRY_RUN}" == "yes" ]]; then
    echo "[dry-run] $*"
  else
    echo "$*"
  fi
}

preflight() {
  if [[ "${DRY_RUN}" == "yes" ]]; then
    return 0
  fi
  if ! command -v gh > /dev/null 2>&1; then
    echo "ERROR: the GitHub CLI (gh) is not installed. See https://cli.github.com" >&2
    exit 2
  fi
  if ! gh auth status > /dev/null 2>&1; then
    echo "ERROR: not signed in. Run: gh auth login" >&2
    exit 2
  fi
  if [[ "$(gh api "repos/${REPO}" --jq '.has_issues')" != "true" ]]; then
    echo "ERROR: issues are disabled on ${REPO}. Enable them under Settings > General > Features." >&2
    exit 2
  fi
  EXISTING_TITLES="$(gh issue list --repo "${REPO}" --state all --limit 500 --json title --jq '.[].title')"
  EXISTING_LABELS="$(gh label list --repo "${REPO}" --limit 500 --json name --jq '.[].name')"
}

# ensure_label <name> <colour> <description>
ensure_label() {
  if grep -qxF "$1" <<< "${EXISTING_LABELS}"; then
    say "label      $1 (exists, skipped)"
    return 0
  fi
  say "label      $1"
  if [[ "${DRY_RUN}" == "no" ]]; then
    gh label create "$1" --repo "${REPO}" --color "$2" --description "$3" > /dev/null
  fi
}

ensure_milestone() {
  if [[ "${DRY_RUN}" == "yes" ]]; then
    say "milestone  ${MILESTONE}${DUE:+ (due ${DUE})}"
    return 0
  fi
  if gh api "repos/${REPO}/milestones?state=all&per_page=100" --jq '.[].title' | grep -qxF "${MILESTONE}"; then
    say "milestone  ${MILESTONE} (exists, skipped)"
    return 0
  fi
  local args=(-f "title=${MILESTONE}" -f "description=${MILESTONE_DESCRIPTION}")
  if [[ -n "${DUE}" ]]; then
    args+=(-f "due_on=${DUE}T23:59:59Z")
  fi
  gh api "repos/${REPO}/milestones" "${args[@]}" > /dev/null
  say "milestone  ${MILESTONE} (created)"
}

# create_issue <work item> <title> <comma-separated labels> <assignee or empty>, body on stdin
create_issue() {
  local item="$1" title="[$1] $2" labels="$3" assignee="$4" body
  body="$(cat)"
  if grep -qF "[${item}] " <<< "${EXISTING_TITLES}"; then
    say "issue      ${title} (exists, skipped)"
    SKIPPED=$((SKIPPED + 1))
    return 0
  fi
  say "issue      ${title}  {${labels}}  -> ${assignee:-unassigned}"
  if [[ "${DRY_RUN}" == "no" ]]; then
    local args=(--repo "${REPO}" --title "${title}" --body "${body}" --label "${labels}" --milestone "${MILESTONE}")
    if [[ -n "${assignee}" ]]; then
      args+=(--assignee "${assignee}")
    fi
    gh issue create "${args[@]}" > /dev/null
  fi
  CREATED=$((CREATED + 1))
}

echo "Repository: ${REPO}"
preflight

ensure_label "phase-2" "1d76db" "Phase 2: event pipeline and live push"
ensure_label "story"   "0e8a16" "User story"
ensure_label "enabler" "5319e7" "Technical work that enables stories"
ensure_milestone

create_issue "W4" "US1: Publish a VisitEvent when a visit is booked (5 pts)" "phase-2,story" "${ASSIGNEE_JAWAD}" <<'EOF'
**Story.** As a developer, I want visits-service to publish an event whenever a visit is booked, so other services can react without a direct call.

**Lead:** Jawad. **Points:** 5 (Jawad 3 design and code, Rahul 1 tests, Sneha 1 infra).

## Acceptance criteria
- [ ] Booking returns the same 201 response as today
- [ ] Exactly one valid `VISIT_SCHEDULED` event on `visit-events`, key = `petId`
- [ ] The owner id is included in the event
- [ ] If Kafka is down the booking still succeeds and the error is logged

## Tasks
- [ ] Add Spring Kafka (Spring Boot 4 versions)
- [ ] Map `owners/{ownerId}/pets/{petId}/visits` so the owner id is no longer dropped
- [ ] `VisitEvent` record matching `contracts/visit-event-schema.json`
- [ ] `VisitEventPublisher` sending after the database commit
- [ ] Kafka settings for local (`localhost:29092`) and Docker (`kafka:9092`); compose dependency

## Tests
- [ ] Publisher unit test with a mock
- [ ] Event validates against the schema, with format checking switched on
- [ ] Kafka-down test still returns 201

See `docs/spikes/baseline-limitations.md`, findings 2 and 4.
EOF

create_issue "W5" "US1: Cancel a visit (3 pts)" "phase-2,story" "${ASSIGNEE_JAWAD}" <<'EOF'
**Story.** As front-desk staff, I want to cancel a visit, so the schedule and alerts stay correct.

**Lead:** Jawad. **Points:** 3 (Jawad 2 code, Rahul 1 tests).

## Acceptance criteria
- [ ] A cancel endpoint marks the visit cancelled and never deletes it
- [ ] It publishes `VISIT_CANCELLED`
- [ ] Cancelling twice is rejected cleanly

## Tasks
- [ ] `status` column, default `SCHEDULED`, in the HSQLDB and MySQL schemas
- [ ] `PUT /owners/{ownerId}/pets/{petId}/visits/{visitId}/cancel`
- [ ] Publish the event after commit
- [ ] Cancel button in the UI

## Tests
- [ ] Endpoint tests: cancel, double cancel, not found
- [ ] Event test

Depends on W4.
EOF

create_issue "W6" "US2: Visit type and alert classification (5 pts)" "phase-2,story" "${ASSIGNEE_JAWAD}" <<'EOF'
**Story.** As front-desk staff, I want to choose a visit type when booking, and as an administrator I want each visit classified as emergency or routine, so urgent visits stand out.

**Lead:** Jawad. **Points:** 5 (Jawad 3 code, Rahul 1 tests, Sneha 1 config).

## Acceptance criteria
- [ ] Type dropdown in the visit form, `ROUTINE` by default
- [ ] The type is stored and included in the event
- [ ] `EMERGENCY` and `URGENT_CARE` give EMERGENCY, anything else ROUTINE
- [ ] The urgent list changes without a code change

## Tasks
- [ ] `visit_type` column, `VARCHAR(40) NOT NULL DEFAULT 'ROUTINE'`, in the HSQLDB and MySQL schemas
- [ ] `Visit.visitType` with validation (upper case, as the contract requires)
- [ ] Form dropdown
- [ ] `AlertClassifier` reading `petclinic.alerts.urgent-types`
- [ ] Urgent-types setting in the config repository

## Tests
- [ ] Classifier test per type, plus an unknown type
- [ ] REST test with a type

Can start alongside W4.
EOF

create_issue "W7" "US3: Alert service with a permanent audit record (8 pts)" "phase-2,story" "${ASSIGNEE_JAWAD}" <<'EOF'
**Story.** As a clinic administrator, I want every alert stored as a permanent audit record I can search, so I can see what happened and when.

**Lead:** Jawad. **Points:** 8 (Jawad 4 code, Rahul 3 tests, Sneha 1 infra).

## Acceptance criteria
- [ ] New service registered in Eureka and reachable at `/api/alert/**`
- [ ] One record per event; a redelivered event adds nothing
- [ ] Records are never updated or deleted
- [ ] Search by pet, owner, type and date
- [ ] Failing events reach `visit-events.DLT` after 3 retries

## Tasks
- [ ] New Maven module `notification-alert-service` (port 8085)
- [ ] PostgreSQL container in compose
- [ ] `AuditRecord` entity with a unique `event_id`
- [ ] `@KafkaListener` in group `notification-group`
- [ ] Error handler with dead-letter publishing
- [ ] Gateway route
- [ ] `GET /audit?petId=&ownerId=&type=&from=&to=`

## Tests
- [ ] Testcontainers test with Kafka and PostgreSQL: one row per event, duplicate ignored, bad event on the DLT

Depends on W4 and W6.
EOF

create_issue "W8" "US4: Push new alerts to open browsers over WebSocket (5 pts)" "phase-2,story" "${ASSIGNEE_JAWAD}" <<'EOF'
**Story.** As front-desk staff, I want new bookings pushed to my open screen instantly, so I never need to refresh.

**Lead:** Jawad. **Points:** 5 (Jawad 3 code, Rahul 1 tests, Sneha 1 infra).

## Acceptance criteria
- [ ] Every open browser receives the classified alert within about 1 second of a booking
- [ ] Nothing breaks when no browser is connected

## Tasks
- [ ] WebSocket starter in notification-alert-service
- [ ] STOMP endpoint `/ws`
- [ ] Send each saved alert to `/topic/notifications`
- [ ] Gateway WebSocket route

## Tests
- [ ] Integration test with a STOMP client: subscribe, publish an event, receive the alert

Depends on W7.
EOF

create_issue "W9" "Enabler: Run our own images in Docker (2 pts)" "phase-2,enabler" "${ASSIGNEE_SNEHA}" <<'EOF'
**Story.** As the team, we want Docker to run our own code, so the demo shows our changes and not the upstream images.

**Lead:** Sneha. **Points:** 2.

## Acceptance criteria
- [ ] Changed services and the new service run from images built locally
- [ ] PostgreSQL in compose
- [ ] Smoke test extended to the new service

## Tasks
- [ ] Build images with `./mvnw clean install -P buildDocker`
- [ ] Point compose at the local images
- [ ] Extend the smoke test and SETUP.md

## Tests
- [ ] Phase 2 smoke test on every laptop

Can run alongside W4 to W8.
EOF

echo
if [[ "${DRY_RUN}" == "yes" ]]; then
  echo "Dry run: ${CREATED} issues would be created. Nothing was changed."
else
  echo "Done: ${CREATED} issues created, ${SKIPPED} already existed."
  echo "https://github.com/${REPO}/issues"
fi
