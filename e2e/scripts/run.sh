#!/usr/bin/env bash
set -euo pipefail

E2E_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_ID="${APP_ID:-com.sloy.sevibus.debug}"
EXPECTED_HOST="${EXPECTED_HOST:-appdev-vd4mgiw7ma-no.a.run.app}"
DEVICE="${DEVICE:-$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')}"
REPORT_DIR="${REPORT_DIR:-$E2E_DIR/build/reports}"

if [[ -z "$DEVICE" ]]; then
  echo "No connected device found" >&2
  exit 1
fi

if ! adb -s "$DEVICE" shell pm list packages | grep -q "package:$APP_ID$"; then
  echo "$APP_ID is not installed on $DEVICE. Install the build under test first." >&2
  exit 1
fi

LOCALE="$(adb -s "$DEVICE" shell getprop persist.sys.locale | tr -d '\r')"
LOCALE="${LOCALE:-$(adb -s "$DEVICE" shell getprop ro.product.locale | tr -d '\r')}"
if [[ "$LOCALE" != en-* ]]; then
  echo "Device locale is '$LOCALE'. Flows use English copies, set the device language to English (en-US)." >&2
  exit 1
fi

if lsof -nP -iTCP:7001 -sTCP:LISTEN >/dev/null 2>&1 && ps -o command= -p "$(lsof -nP -iTCP:7001 -sTCP:LISTEN -t | head -1)" | grep -q "maestro.cli.AppKt mcp"; then
  echo "A Maestro MCP server is holding port 7001. Stop it (or disconnect it in /mcp) before running the CLI." >&2
  exit 1
fi

mkdir -p "$REPORT_DIR"

TARGET="${1:-$E2E_DIR}"
shift || true

FAILED_CONFIG="$REPORT_DIR/failed-config.yaml"

echo "Running maestro..."
status=0
maestro --device "$DEVICE" test \
  -e APP_ID="$APP_ID" \
  -e EXPECTED_HOST="$EXPECTED_HOST" \
  --format junit \
  --output "$REPORT_DIR/junit.xml" \
  --test-output-dir "$REPORT_DIR/artifacts" \
  "$@" \
  "$TARGET" || status=$?

# Write a workspace config with the flows that failed, so they can be retried without running the whole suite.
# The junit "file" attribute is relative to the directory maestro ran from; config paths are relative to E2E_DIR.
failed_flows=()
if [[ -f "$REPORT_DIR/junit.xml" ]]; then
  while IFS= read -r file; do
    [[ "$file" = /* ]] || file="$PWD/$file"
    file="$(cd "$(dirname "$file")" && pwd)/$(basename "$file")"
    failed_flows+=("${file#"$E2E_DIR"/}")
  done < <(grep '<testcase ' "$REPORT_DIR/junit.xml" | grep -v 'status="SUCCESS"' | sed -n 's/.* file="\([^"]*\)".*/\1/p')
fi

if (( ${#failed_flows[@]} > 0 )); then
  {
    echo "flows:"
    printf '  - "%s"\n' "${failed_flows[@]}"
    echo "executionOrder:"
    echo "  continueOnFailure: true"
  } > "$FAILED_CONFIG"
  echo
  echo "${#failed_flows[@]} flow(s) failed. Retry them with:"
  echo "  $0 $E2E_DIR --config $FAILED_CONFIG"
else
  rm -f "$FAILED_CONFIG"
fi

exit "$status"
