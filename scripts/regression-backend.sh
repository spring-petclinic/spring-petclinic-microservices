#!/usr/bin/env bash
# Backend regression test (W15): what worked before Phase 2 must keep working, through the
# gateway, with the requests the UI sends. Exits 0 only when all checks pass.
# It adds one test owner, one test pet and one test visit to the running stack.
#
# Usage (from the repository root, with the stack running):
#   ./scripts/regression-backend.sh
#
# Options (environment variables): HOST, NO_COLOR. On Windows, run it from Git Bash or WSL.

set -o nounset
set -o pipefail

HOST="${HOST:-localhost}"
GATEWAY="http://${HOST}:8080"
TIMEOUT=20
JSON='Content-Type: application/json'

if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  GREEN=$'\033[0;32m'; RED=$'\033[0;31m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else
  GREEN=""; RED=""; BOLD=""; RESET=""
fi

PASSED=0
FAILED=0
BODY=""
CODE=""

# call <curl arguments>: sets BODY and CODE
call() {
  local out
  out="$(curl -s --max-time "${TIMEOUT}" -w '\n%{http_code}' "$@" 2> /dev/null)"
  CODE="${out##*$'\n'}"
  BODY="${out%$'\n'*}"
}

# check <name> <expected status> <text the body must contain, or empty> <curl arguments>
check() {
  local name="$1" expected="$2" text="$3" result=0 detail
  shift 3
  call "$@"
  detail="HTTP ${CODE}, expected ${expected}"
  [[ "${CODE}" == "${expected}" ]] || result=1
  if [[ -n "${text}" ]] && ! grep -qF -- "${text}" <<< "${BODY}"; then
    result=1
    detail="${detail}; body lacks ${text}"
  fi
  if (( result == 0 )); then
    PASSED=$((PASSED + 1))
    printf '%s[PASS]%s %-44s %s\n' "${GREEN}" "${RESET}" "${name}" "${detail}"
  else
    FAILED=$((FAILED + 1))
    printf '%s[FAIL]%s %-44s %s\n' "${RED}" "${RESET}" "${name}" "${detail}"
  fi
}

# first_id: the first "id" in the last response. JSON keys are sorted, so nested ids come later.
first_id() {
  grep -o '"id":[0-9]*' <<< "${BODY}" | head -n 1 | cut -d: -f2
}

echo "${BOLD}Backend regression test${RESET} against ${GATEWAY}"

check "UI home page" 200 "<title>" "${GATEWAY}/"
check "list owners" 200 '"lastName":"Franklin"' "${GATEWAY}/api/customer/owners"
check "one owner" 200 '"firstName":"George"' "${GATEWAY}/api/customer/owners/1"
check "owner details with visits" 200 '"description":"rabies shot"' "${GATEWAY}/api/gateway/owners/6"
check "list vets" 200 '"lastName":"Carter"' "${GATEWAY}/api/vet/vets"
check "pet types" 200 '"name":"cat"' "${GATEWAY}/api/customer/petTypes"
check "visits of several pets" 200 '"items"' "${GATEWAY}/api/visit/pets/visits?petId=7,8"
check "visits of one pet" 200 '"petId":7' "${GATEWAY}/api/visit/owners/6/pets/7/visits"

check "register an owner" 201 '"lastName":"Regression"' -H "${JSON}" \
  -d '{"firstName":"Test","lastName":"Regression","address":"1 Test St.","city":"Testville","telephone":"6085550000"}' \
  "${GATEWAY}/api/customer/owners"
OWNER_ID="$(first_id)"

check "edit the owner" 204 "" -X PUT -H "${JSON}" \
  -d '{"firstName":"Test","lastName":"Regression","address":"2 Test St.","city":"Testville","telephone":"6085550000"}' \
  "${GATEWAY}/api/customer/owners/${OWNER_ID:-0}"
check "the edit was saved" 200 '"address":"2 Test St."' "${GATEWAY}/api/customer/owners/${OWNER_ID:-0}"

check "add a pet" 201 '"name":"Regressor"' -H "${JSON}" \
  -d '{"id":0,"name":"Regressor","birthDate":"2020-01-01","typeId":1}' \
  "${GATEWAY}/api/customer/owners/${OWNER_ID:-0}/pets"
PET_ID="$(first_id)"

# The request the form sent before Phase 2: no visit type
check "book a visit as the old form did" 201 '"visitType":"ROUTINE"' -H "${JSON}" \
  -d '{"date":"2026-12-24","description":"regression check"}' \
  "${GATEWAY}/api/visit/owners/${OWNER_ID:-0}/pets/${PET_ID:-0}/visits"
check "the visit keeps its date" 200 '"date":"2026-12-24"' \
  "${GATEWAY}/api/visit/owners/${OWNER_ID:-0}/pets/${PET_ID:-0}/visits"
check "the visit shows on the owner page" 200 '"description":"regression check"' \
  "${GATEWAY}/api/gateway/owners/${OWNER_ID:-0}"

echo
if (( FAILED == 0 )); then
  echo "${GREEN}${BOLD}RESULT: ${PASSED}/${PASSED} PASS${RESET}"
  exit 0
fi
echo "${RED}${BOLD}RESULT: ${PASSED}/$((PASSED + FAILED)) passed, ${FAILED} FAILED${RESET}"
exit 1
