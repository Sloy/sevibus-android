#!/usr/bin/env bash
# Records responses from dev to turn them into fixtures. Integration runs never reach dev, so recording
# first points WireMock at dev, then saves what it proxied.
# Usage:
#   record.sh proxy                    send every request to dev until WireMock restarts
#   record.sh <name> <urlPattern>      save the proxied responses, e.g. record.sh stops '/api/stops'
set -euo pipefail

TESTS_DIR="$(cd "$(dirname "$0")/.." && pwd)"
source "$TESTS_DIR/scripts/wiremock.sh"
MOCK_PORT="${MOCK_PORT:-8089}"

if ! wiremock_port_is_wiremock "$MOCK_PORT"; then
  echo "No WireMock on port $MOCK_PORT. Start one first (run.sh integration starts it)." >&2
  exit 1
fi

if [[ $# -eq 1 && "$1" == "proxy" ]]; then
  curl -sf -X POST "http://localhost:$MOCK_PORT/__admin/mappings" \
    -H 'Content-Type: application/json' -d @"$TESTS_DIR/mocks/proxy-dev.json" >/dev/null
  echo "WireMock on port $MOCK_PORT now proxies every request to dev. Drive the app, then run: $0 <name> <urlPattern>"
  exit 0
fi

if [[ $# -ne 2 ]]; then
  sed -n '2,6p' "$0" >&2
  exit 1
fi

out="$TESTS_DIR/build/recordings/$1.json"
mkdir -p "$(dirname "$out")"
curl -sf -X POST "http://localhost:$MOCK_PORT/__admin/recordings/snapshot" \
  -H 'Content-Type: application/json' \
  -d "{\"filters\":{\"urlPattern\":\"$2\"},\"persist\":false,\"repeatsAsScenarios\":false}" > "$out"
echo "Recorded $(jq '.mappings | length' "$out") mapping(s) to $out"
echo "Trim it, anonymize personal data, add scenarioName/requiredScenarioState and move it into tests/mocks/."
