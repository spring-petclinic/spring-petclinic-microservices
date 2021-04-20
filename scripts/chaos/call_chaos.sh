#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

ROOT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function usage {
    echo "usage: $0: <customers|visits|vets> <attacks_enable_exception|attacks_enable_killapplication|attacks_enable_latency|attacks_enable_memory|watcher_enable_component|watcher_enable_controller|watcher_enable_repository|watcher_enable_restcontroller|watcher_enable_service|watcher_disable>"
    echo "First pick either customers, visits or vets"
    echo "Then pick what to enable. Order matters!"
    echo "Example"
    echo "./scripts/chaos/call_chaos.sh visits attacks_enable_exception watcher_enable_restcontroller"
    exit 1
}

if [[ $# -lt 2 ]]; then
    usage
fi

export PORT="${PORT:-}"

while [[ $# > 0 ]]
do
key="$1"
case $1 in
    customers)
        PORT=8081
        ;;
    visits)
        PORT=8082
        ;;
    vets)
        PORT=8083
        ;;
    attacks*)
        ( cd "${ROOT_DIR}" && curl "http://localhost:${PORT}/actuator/chaosmonkey/assaults" -H "Content-Type: application/json" --data @"${1}".json --fail )
        ;;
    watcher*)
        ( cd "${ROOT_DIR}" && curl "http://localhost:${PORT}/actuator/chaosmonkey/watchers" -H "Content-Type: application/json" --data @"${1}".json --fail )
        ;;
    *)
        usage
        ;;
esac
shift
done
