#!/usr/bin/env bash
# PreToolUse hook for the Bash tool. Claude Code passes the tool call as JSON on stdin.
# Exit code 2 blocks the call and returns the stderr message to the agent.
set -euo pipefail

input="$(cat)"
cmd="$(jq -r '.tool_input.command // ""' <<<"$input")"

if grep -Eiq '(^|[;&|[:space:]])(curl|wget|nc|ncat|ssh|scp)([[:space:]]|$)' <<<"$cmd"; then
  echo "Blocked: network commands are not allowed in review runs." >&2
  exit 2
fi

if grep -Eiq '(\.env|\.pem|id_rsa|\.aws/credentials|(^|[[:space:]/])secrets/)' <<<"$cmd"; then
  echo "Blocked: access to secret files is not allowed in review runs." >&2
  exit 2
fi

exit 0