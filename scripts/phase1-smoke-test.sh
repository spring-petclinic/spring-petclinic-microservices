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
#   RETRIES         attempts per check before FAIL        (default: 1; use e.g. 30 right after startup)
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
#   - If a port is already taken on your laptop (Windows often reserves 8081), change the
#     host side of the mapping in docker-compose.yml; customers-service is auto-detected.

set -o nounset
set -o pipefail

HOST="${HOST:-localhost}"
TIMEOUT="${TIMEOUT:-5}"
RETRIES="${RETRIES:-1}"
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

# probe_http <url>: any 2xx/3xx answer counts as reachable
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

# check <name> <probe> [args...]: run a probe (with retries) and record PASS/FAIL
check() {
  local name="$1"; shift
  local attempt=1 detail=""
  TOTAL=$((TOTAL + 1))
  while true; do
    if detail="$("$@")"; then
      PASSED=$((PASSED + 1))
      printf '%s[PASS]%s %2d. %-34s %s\n' "${GREEN}" "${RESET}" "${TOTAL}" "${name}" "${detail}"
      return 0
    fi
    if (( attempt >= RETRIES )); then
      break
    fi
    attempt=$((attempt + 1))
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
check "gateway UI /"               probe_http "http://${HOST}:8080/"
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
echo "${YELLOW}Hint:${RESET} services need 1-2 minutes after 'docker compose up -d'. Retry with RETRIES=30, or check 'docker compose ps' and 'docker compose logs <service>'."
exit 1
