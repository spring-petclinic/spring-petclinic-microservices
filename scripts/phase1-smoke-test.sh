#!/usr/bin/env bash
# Phase 1 smoke test (W1 / Gate 1): 18 checks against the running docker compose stack.
# Exits 0 only when all 18 checks pass.
#
# Usage (from the repository root, after `docker compose up -d`):
#   ./scripts/phase1-smoke-test.sh
#
# Options (environment variables):
#   HOST            host the stack is published on        (default: localhost)
#   CUSTOMERS_PORT  host port of customers-service        (default: read from docker compose, else 8081)
#   TIMEOUT         per-request timeout in seconds        (default: 5)
#   WAIT            total seconds to wait for services    (default: 120; 0 = single attempt, no waiting)
#                   that are still starting, shared by all checks
#   RETRY_DELAY     seconds between attempts              (default: 5)
#   NO_COLOR        set to any value to disable colours
#
# Windows notes:
#   - Run it from Git Bash or WSL 2, not from PowerShell or cmd.exe:
#       Git Bash:   ./scripts/phase1-smoke-test.sh
#       PowerShell: & "C:\Program Files\Git\bin\bash.exe" scripts/phase1-smoke-test.sh
#   - Docker Desktop must be running. From WSL, enable "WSL integration" for your distro
#     in Docker Desktop settings so that `docker` and localhost ports are reachable.
#   - The script must keep LF line endings (enforced by .gitattributes). If you see
#     "$'\r': command not found", run: git add --renormalize . && git checkout -- scripts
#   - Git Bash rewrites arguments that look like Unix paths; the script disables that
#     (MSYS_NO_PATHCONV) for its `docker exec ... /opt/kafka/bin/...` calls only.
#   - If port 8081 is already taken on your laptop (Windows often reserves it), put
#     CUSTOMERS_PORT=18081 in a .env file next to docker-compose.yml; the script detects it.
#   - More detail: SETUP.md

# shellcheck disable=SC2329  # probe_* functions are invoked indirectly through check()
set -o nounset
set -o pipefail

HOST="${HOST:-localhost}"
TIMEOUT="${TIMEOUT:-5}"
WAIT="${WAIT:-120}"
RETRY_DELAY="${RETRY_DELAY:-5}"
EXPECTED_CHECKS=18

if [[ -t 1 && -z "${NO_COLOR:-}" ]]; then
  GREEN=$'\033[0;32m'; RED=$'\033[0;31m'; YELLOW=$'\033[0;33m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else
  GREEN=""; RED=""; YELLOW=""; BOLD=""; RESET=""
fi

PASSED=0
FAILED=0
TOTAL=0
FAILED_NAMES=()

for tool in curl docker; do
  if ! command -v "${tool}" > /dev/null 2>&1; then
    echo "${RED}ERROR${RESET}: '${tool}' is required but was not found on PATH" >&2
    exit 2
  fi
done

# customers-service is the one port people remap, so ask compose where it is published
detect_customers_port() {
  local mapping
  mapping="$(docker compose port customers-service 8081 2> /dev/null | tr -d '\r')"
  if [[ -n "${mapping}" ]]; then
    echo "${mapping##*:}"
  else
    echo 8081
  fi
}
CUSTOMERS_PORT="${CUSTOMERS_PORT:-$(detect_customers_port)}"

# --- probes: each returns 0 on success and prints a short detail line ---

# probe_http <url>: any 2xx/3xx answer counts as reachable. The body is not read: Spring's
# /actuator/health answers 503 when the service is DOWN, so the status code is enough.
probe_http() {
  local url="$1" code
  code="$(curl -s -o /dev/null -w '%{http_code}' --max-time "${TIMEOUT}" "${url}" 2> /dev/null)" || code="000"
  echo "HTTP ${code} ${url}"
  [[ "${code}" =~ ^[23][0-9][0-9]$ ]]
}

# probe_tcp <port>: plain TCP connect, for ports that do not speak HTTP
probe_tcp() {
  local port="$1"
  echo "tcp ${HOST}:${port}"
  if command -v timeout > /dev/null 2>&1; then
    timeout "${TIMEOUT}" bash -c "exec 3<> /dev/tcp/${HOST}/${port}" 2> /dev/null
  else
    bash -c "exec 3<> /dev/tcp/${HOST}/${port}" 2> /dev/null
  fi
}

# probe_topic <topic> <partitions>: topic exists on the broker with the expected partition count
probe_topic() {
  local topic="$1" expected="$2" described count
  described="$(MSYS_NO_PATHCONV=1 docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 \
    --describe --topic "${topic}" 2> /dev/null | tr -d '\r')" || described=""
  count="$(sed -n 's/.*PartitionCount:[[:space:]]*\([0-9][0-9]*\).*/\1/p' <<< "${described}" | head -n 1)"
  echo "topic ${topic}: ${count:-missing} partition(s), expected ${expected}"
  [[ "${count:-}" == "${expected}" ]]
}

# probe_topics: both topics created by kafka-init are present
probe_topics() {
  local main dlt rc=0
  main="$(probe_topic visit-events 3)" || rc=1
  dlt="$(probe_topic visit-events.DLT 1)" || rc=1
  echo "${main}; ${dlt}"
  return "${rc}"
}

# probe_roundtrip: produce one message to the smoke-test topic and consume it back
probe_roundtrip() {
  local id got
  id="smoke-$(date +%s)-$$"
  # shellcheck disable=SC2016  # expanded by bash inside the container, not here
  got="$(MSYS_NO_PATHCONV=1 docker exec kafka bash -c '
    BIN=/opt/kafka/bin; BS=kafka:9092; T=smoke-test
    offset="$($BIN/kafka-get-offsets.sh --bootstrap-server $BS --topic $T | cut -d: -f3)" || exit 1
    [ -n "$offset" ] || exit 1
    echo "$1" | $BIN/kafka-console-producer.sh --bootstrap-server $BS --topic $T || exit 1
    $BIN/kafka-console-consumer.sh --bootstrap-server $BS --topic $T --partition 0 --offset "$offset" --max-messages 1 --timeout-ms 10000
  ' _ "${id}" 2> /dev/null | tr -d '\r')" || got=""
  if [[ "${got}" == "${id}" ]]; then
    echo "produced and consumed ${id} on topic smoke-test"
    return 0
  fi
  echo "message ${id} not read back from topic smoke-test (run 'docker compose up -d kafka-init' if the topic is missing)"
  return 1
}

# check <name> <probe> [args...]: run a probe and record PASS/FAIL. A failing probe is retried
# until the shared WAIT budget is used up, so a dead service cannot stall the run for long.
check() {
  local name="$1"; shift
  local detail=""
  TOTAL=$((TOTAL + 1))
  while true; do
    if detail="$("$@")"; then
      PASSED=$((PASSED + 1))
      printf '%s[PASS]%s %2d. %-34s %s\n' "${GREEN}" "${RESET}" "${TOTAL}" "${name}" "${detail}"
      return 0
    fi
    if (( SECONDS + RETRY_DELAY > WAIT )); then
      break
    fi
    sleep "${RETRY_DELAY}"
  done
  FAILED=$((FAILED + 1))
  FAILED_NAMES+=("${name}")
  printf '%s[FAIL]%s %2d. %-34s %s\n' "${RED}" "${RESET}" "${TOTAL}" "${name}" "${detail}"
  return 0
}

section() {
  printf '\n%s%s%s\n' "${BOLD}" "$1" "${RESET}"
}

echo "${BOLD}Phase 1 smoke test${RESET} against ${HOST} (customers-service on port ${CUSTOMERS_PORT})"

section "Platform services"
check "config-server :8888"        probe_http "http://${HOST}:8888/actuator/health"
check "discovery-server :8761"     probe_http "http://${HOST}:8761/actuator/health"
check "api-gateway :8080"          probe_http "http://${HOST}:8080/actuator/health"
check "admin-server :9090"         probe_http "http://${HOST}:9090/actuator/health"

section "Business services"
check "customers-service :${CUSTOMERS_PORT}" probe_http "http://${HOST}:${CUSTOMERS_PORT}/actuator/health"
check "visits-service :8082"       probe_http "http://${HOST}:8082/actuator/health"
check "vets-service :8083"         probe_http "http://${HOST}:8083/actuator/health"
check "genai-service :8084"        probe_http "http://${HOST}:8084/actuator/health"

section "Gateway routes"
check "gateway -> customers"       probe_http "http://${HOST}:8080/api/customer/owners"
check "gateway -> vets"            probe_http "http://${HOST}:8080/api/vet/vets"
check "gateway -> visits"          probe_http "http://${HOST}:8080/api/visit/pets/visits?petId=1"

section "Observability"
check "tracing-server (Zipkin) :9411" probe_http "http://${HOST}:9411/health"
check "grafana-server :3030"       probe_http "http://${HOST}:3030/api/health"
check "prometheus-server :9091"    probe_http "http://${HOST}:9091/-/healthy"

section "Kafka"
check "kafka broker :29092"        probe_tcp 29092
check "kafka-ui :8090"             probe_http "http://${HOST}:8090/actuator/health"
check "topics visit-events + DLT"  probe_topics
check "kafka produce/consume"      probe_roundtrip

echo
if (( TOTAL != EXPECTED_CHECKS )); then
  echo "${RED}ERROR${RESET}: ran ${TOTAL} checks, expected ${EXPECTED_CHECKS} - the script is out of sync" >&2
  exit 2
fi

if (( FAILED == 0 )); then
  echo "${GREEN}${BOLD}RESULT: ${PASSED}/${TOTAL} PASS${RESET}"
  exit 0
fi

echo "${RED}${BOLD}RESULT: ${PASSED}/${TOTAL} passed, ${FAILED} FAILED${RESET}"
for name in "${FAILED_NAMES[@]}"; do
  echo "  ${RED}-${RESET} ${name}"
done
echo "${YELLOW}Hint:${RESET} services need 1-2 minutes after 'docker compose up -d'. Raise WAIT (seconds), or check 'docker compose ps' and 'docker compose logs <service>'."
exit 1
