#!/usr/bin/env bash
# Phase 2 smoke test (W9 / Gate 2): the 18 Phase 1 checks, then the event pipeline on the running
# stack. It books and cancels one test visit through the gateway. Exits 0 only when all pass.
#
# Usage (from the repository root, after building the images and `docker compose up -d`):
#   ./scripts/phase2-smoke-test.sh
#
# Options (environment variables): HOST, WAIT and NO_COLOR as in phase1-smoke-test.sh.
# See the Windows notes in phase1-smoke-test.sh and SETUP.md.

set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOST="${HOST:-localhost}"
GATEWAY="http://${HOST}:8080"
TIMEOUT=20
OWNER_ID=1
PET_ID=1

if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  GREEN=$'\033[0;32m'; RED=$'\033[0;31m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else
  GREEN=""; RED=""; BOLD=""; RESET=""
fi

PASSED=0
FAILED=0

"${SCRIPT_DIR}/phase1-smoke-test.sh"
PHASE1_STATUS=$?

report() {
  local result="$1" name="$2" detail="$3"
  if [[ "${result}" == "0" ]]; then
    PASSED=$((PASSED + 1))
    printf '%s[PASS]%s %-38s %s\n' "${GREEN}" "${RESET}" "${name}" "${detail}"
  else
    FAILED=$((FAILED + 1))
    printf '%s[FAIL]%s %-38s %s\n' "${RED}" "${RESET}" "${name}" "${detail}"
  fi
}

http_status() {
  curl -s -o /dev/null -w '%{http_code}' --max-time "${TIMEOUT}" "$@" 2> /dev/null
}

check_status() {
  local name="$1" expected="$2" code
  shift 2
  code="$(http_status "$@")"
  [[ "${code}" == "${expected}" ]]
  report "$?" "${name}" "HTTP ${code}, expected ${expected}"
}

# audit_count <event type>: audit rows of this run's test visit with that event type
audit_count() {
  curl -s --max-time "${TIMEOUT}" "${GATEWAY}/api/alert/audit?petId=${PET_ID}" 2> /dev/null \
    | tr '{' '\n' | grep -F "${DESCRIPTION}" | grep -cF "\"eventType\":\"$1\""
}

# await_audit <name> <event type>: exactly one row within 15 seconds
await_audit() {
  local name="$1" type="$2" count=0 attempt
  for attempt in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15; do
    count="$(audit_count "${type}")"
    [[ "${count}" -ge 1 ]] && break
    sleep 1
  done
  # a second look, so that a duplicate row arriving just after the first would be seen
  sleep 2
  count="$(audit_count "${type}")"
  [[ "${count}" == "1" ]]
  report "$?" "${name}" "${count} row(s) after ${attempt}s, expected 1"
}

printf '\n%sPhase 2: event pipeline%s\n' "${BOLD}" "${RESET}"

check_status "notification-alert-service :8085" 200 "http://${HOST}:8085/actuator/health"
check_status "gateway -> audit search" 200 "${GATEWAY}/api/alert/audit?petId=${PET_ID}"
check_status "gateway -> WebSocket /ws" 101 --http1.1 --max-time 5 \
  -H 'Connection: Upgrade' -H 'Upgrade: websocket' -H 'Sec-WebSocket-Version: 13' \
  -H 'Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==' "${GATEWAY}/ws"

DESCRIPTION="phase2-smoke-$(date +%s)-$$"
RESPONSE="$(curl -s --max-time "${TIMEOUT}" -w '\n%{http_code}' -H 'Content-Type: application/json' \
  -d "{\"date\":\"2026-12-01\",\"description\":\"${DESCRIPTION}\",\"visitType\":\"URGENT_CARE\"}" \
  "${GATEWAY}/api/visit/owners/${OWNER_ID}/pets/${PET_ID}/visits" 2> /dev/null)"
BOOKED="$(tail -n 1 <<< "${RESPONSE}")"
if [[ "${BOOKED}" == "201" ]]; then
  report 0 "book a visit" "HTTP ${BOOKED}, expected 201"
else
  report 1 "book a visit" "HTTP ${BOOKED}, expected 201"
fi
VISIT_ID="$(sed -n 's/.*"id":\([0-9][0-9]*\).*/\1/p' <<< "${RESPONSE}" | head -n 1)"

await_audit "booking -> one audit row" VISIT_SCHEDULED

SEVERITY_ROW="$(curl -s --max-time "${TIMEOUT}" "${GATEWAY}/api/alert/audit?petId=${PET_ID}" 2> /dev/null \
  | tr '{' '\n' | grep -F "${DESCRIPTION}" | head -n 1)"
grep -qF '"severity":"EMERGENCY"' <<< "${SEVERITY_ROW}"
report "$?" "URGENT_CARE classified as EMERGENCY" "from the audit row"

CANCEL_URL="${GATEWAY}/api/visit/owners/${OWNER_ID}/pets/${PET_ID}/visits/${VISIT_ID:-0}/cancel"
check_status "cancel the visit" 200 -X PUT "${CANCEL_URL}"
await_audit "cancelling -> one audit row" VISIT_CANCELLED
check_status "cancelling twice is rejected" 409 -X PUT "${CANCEL_URL}"

echo
if (( PHASE1_STATUS == 0 && FAILED == 0 )); then
  echo "${GREEN}${BOLD}RESULT: Phase 1 18/18 and Phase 2 ${PASSED}/$((PASSED + FAILED)) PASS${RESET}"
  exit 0
fi
echo "${RED}${BOLD}RESULT: Phase 1 exit status ${PHASE1_STATUS}; Phase 2 ${PASSED}/$((PASSED + FAILED)) passed, ${FAILED} FAILED${RESET}"
exit 1
