#!/usr/bin/env bash
# Saves the responses WireMock proxied for a URL pattern, to turn them into fixtures.
# Usage: record.sh <name> <urlPattern>   e.g. record.sh stops '/api/stops'
set -euo pipefail

E2E_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$E2E_DIR/scripts/wiremock.sh"
MOCK_PORT="${MOCK_PORT:-8089}"

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <name> <urlPattern>" >&2
  exit 1
fi
if ! wiremock_port_is_wiremock "$MOCK_PORT"; then
  echo "No WireMock on port $MOCK_PORT. Start one and drive the app against it first." >&2
  exit 1
fi

out="$E2E_DIR/build/recordings/$1.json"
mkdir -p "$(dirname "$out")"
curl -sf -X POST "http://localhost:$MOCK_PORT/__admin/recordings/snapshot" \
  -H 'Content-Type: application/json' \
  -d "{\"filters\":{\"urlPattern\":\"$2\"},\"persist\":false,\"repeatsAsScenarios\":false}" > "$out"
echo "Recorded $(jq '.mappings | length' "$out") mapping(s) to $out"
echo "Trim it, anonymize personal data, add scenarioName/requiredScenarioState and move it into e2e/mocks/."
