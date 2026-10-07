#!/usr/bin/env bash
set -euo pipefail

E2E_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_ID="${APP_ID:-com.sloy.sevibus.debug}"
EXPECTED_HOST="${EXPECTED_HOST:-appdev-vd4mgiw7ma-no.a.run.app}"
DEVICE="${DEVICE:-$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')}"
REPORT_DIR="${REPORT_DIR:-$E2E_DIR/build/reports}"
# The map is disabled by default: drawing its markers keeps the device busy and makes flows slow and flaky.
# No flow uses it. MAP_MODE=full brings it back.
MAP_MODE="${MAP_MODE:-disabled}"

if [[ -z "$DEVICE" ]]; then
  echo "No connected device found" >&2
  exit 1
fi

if ! adb -s "$DEVICE" shell pm list packages | grep -q "package:$APP_ID$"; then
  echo "$APP_ID is not installed on $DEVICE. Install the build under test first." >&2
  exit 1
fi

# A fresh install runs interpreted until the device compiles it in the background, and every cold start takes ~20s
# on an emulator. Compile it ahead of time: it takes ~30s after an install and is a no-op afterwards.
echo "Compiling $APP_ID ahead of time..."
adb -s "$DEVICE" shell cmd package compile -m speed "$APP_ID" >/dev/null

LOCALE="$(adb -s "$DEVICE" shell getprop persist.sys.locale | tr -d '\r')"
LOCALE="${LOCALE:-$(adb -s "$DEVICE" shell getprop ro.product.locale | tr -d '\r')}"
if [[ "$LOCALE" != es-* ]]; then
  echo "Device locale is '$LOCALE'. Flows use Spanish copies. Set the device language to Spanish (es-ES)." >&2
  echo "On an emulator: Settings > System > Languages > Add a language > Espanol (Espana), moved above English (see README)." >&2
  exit 1
fi

# The device must be awake and unlocked. A pattern or PIN can't be bypassed from here.
adb -s "$DEVICE" shell input keyevent KEYCODE_WAKEUP
adb -s "$DEVICE" shell wm dismiss-keyguard >/dev/null 2>&1 || true
sleep 1
if adb -s "$DEVICE" shell dumpsys window | grep -qE 'mDreamingLockscreen=true|isKeyguardShowing=true|mShowingLockscreen=true'; then
  echo "Device $DEVICE is locked. Unlock it and run again." >&2
  exit 1
fi

# Keep the screen on for the whole run, otherwise the device locks and every flow fails.
# stay_on_while_plugged_in doesn't work over wireless adb, so the screen timeout is extended instead.
OLD_SCREEN_TIMEOUT="$(adb -s "$DEVICE" shell settings get system screen_off_timeout | tr -d '\r')"
adb -s "$DEVICE" shell settings put system screen_off_timeout 2147483647
echo "Screen timeout extended for the run. If the run is killed, restore it with:"
echo "  adb -s $DEVICE shell settings put system screen_off_timeout $OLD_SCREEN_TIMEOUT"
restore_screen_timeout() {
  adb -s "$DEVICE" shell settings put system screen_off_timeout "$OLD_SCREEN_TIMEOUT" >/dev/null 2>&1 || true
}

# Animations keep the screen changing, and Maestro waits for it to settle before and after every tap.
# Maestro's disableAnimations config only applies to Maestro Cloud, so the scales are set here.
ANIMATION_SCALES=(window_animation_scale transition_animation_scale animator_duration_scale)
OLD_ANIMATION_SCALES=()
for scale in "${ANIMATION_SCALES[@]}"; do
  OLD_ANIMATION_SCALES+=("$(adb -s "$DEVICE" shell settings get global "$scale" | tr -d '\r')")
  adb -s "$DEVICE" shell settings put global "$scale" 0
done
restore_animations() {
  local i value
  for i in "${!ANIMATION_SCALES[@]}"; do
    value="${OLD_ANIMATION_SCALES[$i]}"
    # A scale that was never set reads as "null" and defaults to 1
    [[ "$value" == "null" ]] && value=1
    adb -s "$DEVICE" shell settings put global "${ANIMATION_SCALES[$i]}" "$value" >/dev/null 2>&1 || true
  done
}
echo "Animations disabled for the run. If the run is killed, restore them in Developer options."
# Restore the device if the script exits before the full cleanup trap below is set
trap 'restore_screen_timeout; restore_animations' EXIT

if lsof -nP -iTCP:7001 -sTCP:LISTEN >/dev/null 2>&1 && ps -o command= -p "$(lsof -nP -iTCP:7001 -sTCP:LISTEN -t | head -1)" | grep -q "maestro.cli.AppKt mcp"; then
  echo "A Maestro MCP server is holding port 7001. Stop it (or disconnect it in /mcp) before running the CLI." >&2
  exit 1
fi

mkdir -p "$REPORT_DIR"

source "$E2E_DIR/scripts/wiremock.sh"
wiremock_start "$E2E_DIR/mocks" "$REPORT_DIR/wiremock.log"

# Mocked flows leave the host override pointing at the local WireMock, which is gone after the run.
# Clear it so the app is not left broken. The app is stopped first so it does not write its memory state back.
clear_mock_host_override() {
  adb -s "$DEVICE" shell am force-stop "$APP_ID" >/dev/null 2>&1 || true
  adb -s "$DEVICE" shell "run-as $APP_ID sed -i 's|&quot;hostOverride&quot;:&quot;http://localhost:[0-9]*&quot;|\&quot;hostOverride\&quot;:null|' shared_prefs/debug_menu.xml" >/dev/null 2>&1 || true
}
trap 'wiremock_dump_requests "$REPORT_DIR/wiremock-requests.json"; wiremock_stop; clear_mock_host_override; restore_screen_timeout; restore_animations' EXIT
adb -s "$DEVICE" reverse "tcp:$MOCK_PORT" "tcp:$MOCK_PORT" >/dev/null

TARGET="${1:-$E2E_DIR}"
shift || true

FAILED_CONFIG="$REPORT_DIR/failed-config.yaml"

# The junit format hides the live step-by-step output. A single flow file runs with the interactive output instead.
report_args=(--format junit --output "$REPORT_DIR/junit.xml")
if [[ -f "$TARGET" ]]; then
  report_args=()
  rm -f "$REPORT_DIR/junit.xml"
fi

echo "Running maestro..."
status=0
maestro --device "$DEVICE" test \
  -e APP_ID="$APP_ID" \
  -e EXPECTED_HOST="$EXPECTED_HOST" \
  -e MOCK_PORT="$MOCK_PORT" \
  -e MAP_MODE="$MAP_MODE" \
  ${report_args[@]+"${report_args[@]}"} \
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
