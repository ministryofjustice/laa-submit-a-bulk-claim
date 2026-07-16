#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${1:?namespace required}"
SERVICE_NAME="${2:?service name required}"
LOCAL_PORT="${3:?local port required}"
REMOTE_PORT="${4:-$LOCAL_PORT}"
LOG_FILE="${5:-pf-${LOCAL_PORT}.log}"
MAX_RESTARTS="${6:-20}"
RESTART_DELAY_SECONDS="${7:-2}"

cleanup() {
  if [[ -n "${PF_PID:-}" ]] && kill -0 "$PF_PID" 2>/dev/null; then
    kill "$PF_PID" 2>/dev/null || true
    wait "$PF_PID" 2>/dev/null || true
  fi
}

wait_for_local_port() {
  for _ in {1..30}; do
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/127.0.0.1/${LOCAL_PORT}" 2>/dev/null; then
      return 0
    fi
    sleep 1
  done
  return 1
}

trap cleanup EXIT INT TERM

restarts=0

while true; do
  echo "[watchdog] starting port-forward for service/${SERVICE_NAME} (${LOCAL_PORT} -> ${REMOTE_PORT})"
  kubectl port-forward -n "$NAMESPACE" "service/${SERVICE_NAME}" "${LOCAL_PORT}:${REMOTE_PORT}" >"$LOG_FILE" 2>&1 &
  PF_PID=$!

  if wait_for_local_port; then
    echo "[watchdog] port-forward is ready on localhost:${LOCAL_PORT}"
  else
    echo "[watchdog] port-forward did not become ready" >&2
    tail -n 50 "$LOG_FILE" >&2 || true
    kill "$PF_PID" 2>/dev/null || true
    wait "$PF_PID" 2>/dev/null || true
    ((restarts++))
    if (( restarts > MAX_RESTARTS )); then
      echo "[watchdog] exceeded max restarts (${MAX_RESTARTS})" >&2
      exit 1
    fi
    sleep "$RESTART_DELAY_SECONDS"
    continue
  fi

  wait "$PF_PID"
  exit_code=$?
  echo "[watchdog] port-forward exited with status ${exit_code}" >&2

  if [[ $exit_code -eq 0 ]]; then
    exit 0
  fi

  tail -n 50 "$LOG_FILE" >&2 || true
  ((restarts++))
  if (( restarts > MAX_RESTARTS )); then
    echo "[watchdog] exceeded max restarts (${MAX_RESTARTS})" >&2
    exit 1
  fi

  sleep "$RESTART_DELAY_SECONDS"
done