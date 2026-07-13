#!/usr/bin/env bash
set -euo pipefail

HOST_IP="${HOST_IP:-127.0.0.1}"
HOSTS_FILE="${HOSTS_FILE:-/etc/hosts}"

if [ "$#" -eq 0 ]; then
  echo "Usage: $0 <alias> [<alias> ...]" >&2
  exit 1
fi

ALIASES=("$@")

append_alias() {
  local alias="$1"

  if grep -qE "(^|[[:space:]])${alias}([[:space:]]|$)" "$HOSTS_FILE"; then
    echo "${alias} already present in ${HOSTS_FILE}, skipping"
    return
  fi

  echo "Adding ${alias} -> ${HOST_IP} to ${HOSTS_FILE}"
  if [ -w "$HOSTS_FILE" ]; then
    echo "${HOST_IP} ${alias}" >> "$HOSTS_FILE"
  else
    echo "${HOST_IP} ${alias}" | sudo tee -a "$HOSTS_FILE" >/dev/null
  fi
}

for alias in "${ALIASES[@]}"; do
  append_alias "$alias"
done