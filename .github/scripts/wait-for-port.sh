#!/usr/bin/env bash
# Wait until each provided port on localhost accepts TCP connections.
# Usage: ./scripts/wait-for-port.sh <port> <name> [<port> <name> ...]

wait_for_port() {
  local port=$1
  local name=$2

  for _ in {1..30}; do
    if timeout 1 bash -c "cat < /dev/null > /dev/tcp/127.0.0.1/${port}" 2>/dev/null; then
      echo "[$name] Port is ready on localhost:${port}"
      return 0
    fi
    sleep 2
  done

  echo "[$name] Port ${port} did not open in time" >&2
  return 1
}

if [[ $# -lt 2 || $(( $# % 2 )) -ne 0 ]]; then
  echo "Usage: $0 <port> <name> [<port> <name> ...]" >&2
  exit 1
fi

while [[ $# -gt 0 ]]; do
  port=$1
  name=$2
  shift 2
  wait_for_port "$port" "$name"
done
