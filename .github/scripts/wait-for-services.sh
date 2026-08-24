#!/usr/bin/env bash
set -euo pipefail

# Waits for actuator health to be UP for submit and data claims services.
# Usage: ./wait-for-services.sh [max_attempts] [sleep_seconds]

MAX_ATTEMPTS="${1:-60}"
SLEEP_SECONDS="${2:-2}"

wait_for_health() {
  local port="$1"
  local name="$2"
  local url="http://127.0.0.1:${port}/actuator/health"
  local attempt
  local body

  for ((attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)); do
    body="$(curl --silent --show-error --max-time 3 "$url" || true)"
    if [[ "$body" == *'"status":"UP"'* ]]; then
      echo "[$name] Healthy at $url"
      return 0
    fi

    echo "[$name] Waiting for health at $url (attempt ${attempt}/${MAX_ATTEMPTS})"
    sleep "$SLEEP_SECONDS"
  done

  echo "[$name] Timed out waiting for health at $url" >&2
  return 1
}

wait_for_health 8182 "submit-a-bulk-claim"
wait_for_health 8180 "data-claims-api"
wait_for_health 8181 "data-claims-event-service"