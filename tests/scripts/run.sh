#!/usr/bin/env bash
# Runs the Maestro suites against the installed debug build.
#
# Usage:
#   run.sh                               pick what to run interactively (in a terminal)
#   run.sh integration|e2e|all           run a whole suite
#   run.sh retry                         run again the flows that failed in the last run
#   run.sh <flow file or folder>...      run some flows, e.g. run.sh integration/search e2e
#   run.sh --config <config.yaml>        run the flows listed in a Maestro workspace config
#
# Arguments after -- go to maestro test.
set -euo pipefail

TESTS_DIR="$(cd "$(dirname "$0")/.." && pwd)"
APP_ID="${APP_ID:-com.sloy.sevibus.debug}"
EXPECTED_HOST="${EXPECTED_HOST:-appdev-vd4mgiw7ma-no.a.run.app}"
REPORT_DIR="${REPORT_DIR:-$TESTS_DIR/build/reports}"
# The map is disabled by default: drawing its markers keeps the device busy and makes flows slow and flaky.
# No flow uses it. MAP_MODE=full brings it back.
MAP_MODE="${MAP_MODE:-disabled}"
SUITES=(integration e2e)
FAILED_CONFIG="$REPORT_DIR/failed-config.yaml"
RUN_CONFIG="$REPORT_DIR/run-config.yaml"
usage() {
  sed -n '2,11p' "$0" | sed 's/^# \{0,1\}//' >&2
  exit 1
}

# Flow files of a folder, relative to TESTS_DIR
flows_in() {
  (cd "$TESTS_DIR" && find "$1" -name '*.yaml' | sort)
}

flow_name() {
  sed -n 's/^name: "\(.*\)"$/\1/p' "$TESTS_DIR/$1"
}

# Flows listed in a Maestro workspace config
flows_in_config() {
  sed -n 's/^ *- *"\{0,1\}\([^"]*\)"\{0,1\} *$/\1/p' "$1"
}

# Shows "label<TAB>value" lines read from stdin and prints the values picked.
# Usage: menu single|multi <prompt>. Uses fzf when installed, otherwise a numbered list.
menu() {
  local mode="$1" prompt="$2" entries=() line i choice
  while IFS= read -r line; do entries+=("$line"); done
  if command -v fzf >/dev/null; then
    local fzf_args=(--delimiter=$'\t' --with-nth=1 --prompt="$prompt> " --height=~50% --layout=reverse)
    [[ "$mode" == multi ]] && fzf_args+=(--multi --header='TAB selects several, ENTER runs them')
    printf '%s\n' "${entries[@]}" | fzf "${fzf_args[@]}" | cut -f2
    return
  fi
  i=1
  for line in "${entries[@]}"; do
    printf '%3d) %s\n' "$i" "${line%%$'\t'*}" >&2
    i=$((i + 1))
  done
  if [[ "$mode" == multi ]]; then
    read -r -p "$prompt (numbers separated by spaces): " choice < /dev/tty
  else
    read -r -p "$prompt (number): " choice < /dev/tty
  fi
  for i in $choice; do
    [[ "$i" =~ ^[0-9]+$ ]] && (( i >= 1 && i <= ${#entries[@]} )) || { echo "Invalid choice: $i" >&2; exit 1; }
    echo "${entries[$((i - 1))]}" | cut -f2
  done
}

# One entry per area of the integration suite, labelled with its flow ID prefix (CARDS, FAV, ...)
integration_area_entries() {
  local folder first prefix count
  printf 'All integration flows (%s)\tintegration/\n' "$(flows_in integration | wc -l | tr -d ' ')"
  for folder in $(cd "$TESTS_DIR" && find integration -mindepth 1 -type d | sort); do
    first="$(flows_in "$folder" | head -1)"
    [[ -n "$first" ]] || continue
    prefix="$(flow_name "$first" | cut -d- -f1)"
    count="$(flows_in "$folder" | wc -l | tr -d ' ')"
    printf '%-7s %s (%s)\t%s/\n' "$prefix" "${folder#integration/}" "$count" "$folder"
  done
}

flow_entries() {
  local file
  for file in $(flows_in integration) $(flows_in e2e); do
    printf '%s\t%s\n' "$(flow_name "$file")" "$file"
  done
}

interactive_selection() {
  local choice
  choice="$(
    {
      printf 'Integration tests: every response mocked (%s flows)\tintegration\n' "$(flows_in integration | wc -l | tr -d ' ')"
      printf 'End-to-end tests: real dev backend (%s flows)\te2e\n' "$(flows_in e2e | wc -l | tr -d ' ')"
      printf 'All tests\tall\n'
      [[ -f "$FAILED_CONFIG" ]] && printf 'Retry the failed flows of the last run (%s)\tretry\n' "$(flows_in_config "$FAILED_CONFIG" | wc -l | tr -d ' ')"
      printf 'Pick individual flows\tpick\n'
    } | menu single "Run"
  )"
  case "$choice" in
    integration) integration_area_entries | menu multi "Integration" ;;
    e2e) echo e2e/ ;;
    all) printf '%s/\n' "${SUITES[@]}" ;;
    retry) echo retry ;;
    pick) flow_entries | menu multi "Flows" ;;
    *) exit 1 ;;
  esac
}

add_selection() {
  local path="${1%/}"
  case "$path" in
    all) SELECTION+=("${SUITES[@]/%//}"); return ;;
    retry)
      [[ -f "$FAILED_CONFIG" ]] || { echo "No failed flows to retry: $FAILED_CONFIG doesn't exist" >&2; exit 1; }
      CONFIG="$FAILED_CONFIG"
      return
      ;;
  esac
  # Accept paths relative to the current directory or to TESTS_DIR
  if [[ -e "$path" ]]; then
    path="$(cd "$(dirname "$path")" && pwd)/$(basename "$path")"
    path="${path#"$TESTS_DIR"/}"
  fi
  [[ -e "$TESTS_DIR/$path" ]] || { echo "Not found: $1" >&2; exit 1; }
  [[ -d "$TESTS_DIR/$path" ]] && path="$path/"
  SELECTION+=("$path")
}

# Parse arguments into the flows to run (paths relative to TESTS_DIR) or a config file
SELECTION=()
CONFIG=""
MAESTRO_ARGS=()
while (( $# > 0 )); do
  case "$1" in
    -h|--help) usage ;;
    --config) [[ $# -ge 2 ]] || usage; CONFIG="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"; shift 2 ;;
    --) shift; MAESTRO_ARGS=("$@"); break ;;
    *) add_selection "$1"; shift ;;
  esac
done

if [[ -z "$CONFIG" && ${#SELECTION[@]} -eq 0 ]]; then
  [[ -t 0 ]] || usage
  picked=()
  while IFS= read -r line; do [[ -n "$line" ]] && picked+=("$line"); done < <(interactive_selection)
  (( ${#picked[@]} > 0 )) || { echo "Nothing selected" >&2; exit 1; }
  # "All integration flows" wins over single areas picked with it
  for line in "${picked[@]}"; do [[ "$line" == integration/ ]] && picked=(integration/); done
  for line in "${picked[@]}"; do add_selection "$line"; done
fi

mkdir -p "$REPORT_DIR"

# Everything to run as paths or globs relative to TESTS_DIR, read back from the config when one is given
RUN_PATHS=()
if [[ -n "$CONFIG" ]]; then
  [[ -f "$CONFIG" ]] || { echo "Config not found: $CONFIG" >&2; exit 1; }
  # The failed-flows config is rewritten at the end of the run, so run from a copy
  if [[ "$CONFIG" == "$FAILED_CONFIG" ]]; then
    cp "$FAILED_CONFIG" "$REPORT_DIR/retry-config.yaml"
    CONFIG="$REPORT_DIR/retry-config.yaml"
  fi
  while IFS= read -r line; do RUN_PATHS+=("$line"); done < <(flows_in_config "$CONFIG")
else
  for path in "${SELECTION[@]}"; do
    if [[ "$path" == */ ]]; then RUN_PATHS+=("${path}**"); else RUN_PATHS+=("$path"); fi
  done
fi

NEEDS_MOCKS=false
NEEDS_DEV=false
TOTAL_FLOWS=0
for path in "${RUN_PATHS[@]}"; do
  case "$path" in
    integration/*) NEEDS_MOCKS=true ;;
    e2e/*) NEEDS_DEV=true ;;
    *) echo "Flows must live under integration/ or e2e/: $path" >&2; exit 1 ;;
  esac
  if [[ "$path" == *'**' ]]; then
    TOTAL_FLOWS=$((TOTAL_FLOWS + $(flows_in "${path%\*\*}" | wc -l)))
  else
    TOTAL_FLOWS=$((TOTAL_FLOWS + 1))
  fi
done

# The dev backend scales to zero after a long idle period and takes a few seconds to start.
# Wake it up now, without waiting for it: it's ready by the time the device is prepared.
if $NEEDS_DEV; then
  curl -s -o /dev/null -m 30 "https://$EXPECTED_HOST/admin/health" >/dev/null 2>&1 &
  disown
fi

DEVICE="${DEVICE:-$(adb devices | awk 'NR>1 && $2=="device" {print $1; exit}')}"
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

# Integration flows leave the host override pointing at the local WireMock, which is gone after the run.
# Clear it so the app is not left broken. The app is stopped first so it does not write its memory state back.
clear_mock_host_override() {
  adb -s "$DEVICE" shell am force-stop "$APP_ID" >/dev/null 2>&1 || true
  adb -s "$DEVICE" shell "run-as $APP_ID sed -i 's|&quot;hostOverride&quot;:&quot;http://localhost:[0-9]*&quot;|\&quot;hostOverride\&quot;:null|' shared_prefs/debug_menu.xml" >/dev/null 2>&1 || true
}

MOCK_PORT="${MOCK_PORT:-8089}"
if $NEEDS_MOCKS; then
  source "$TESTS_DIR/scripts/wiremock.sh"
  wiremock_start "$TESTS_DIR/mocks" "$REPORT_DIR/wiremock.log"
  REVERSE_WATCHDOG_PID=""
  trap 'kill "$REVERSE_WATCHDOG_PID" 2>/dev/null || true; wiremock_report_unmatched; wiremock_dump_requests "$REPORT_DIR/wiremock-requests.json"; wiremock_stop; clear_mock_host_override; restore_screen_timeout; restore_animations' EXIT
  # A reused WireMock keeps the requests of earlier runs
  curl -sf -X DELETE "http://localhost:$MOCK_PORT/__admin/requests" >/dev/null || true
  adb -s "$DEVICE" reverse "tcp:$MOCK_PORT" "tcp:$MOCK_PORT" >/dev/null
  # The reverse forward can drop during a run (seen when adb reconnects), and every later request then fails
  # to connect. Put it back whenever it goes missing.
  (
    while sleep 2; do
      adb -s "$DEVICE" reverse --list 2>/dev/null | grep -q "tcp:$MOCK_PORT" \
        || adb -s "$DEVICE" reverse "tcp:$MOCK_PORT" "tcp:$MOCK_PORT" >/dev/null 2>&1 || true
    done
  ) &
  REVERSE_WATCHDOG_PID=$!
  # Not a job of this shell, so killing it at exit doesn't print "Terminated"
  disown
fi

# The junit format hides the live step-by-step output. A single flow file runs with the interactive output instead.
report_args=(--format junit --output "$REPORT_DIR/junit.xml")
target=("$TESTS_DIR")
if [[ -z "$CONFIG" && ${#RUN_PATHS[@]} -eq 1 && -f "$TESTS_DIR/${RUN_PATHS[0]}" ]]; then
  report_args=()
  rm -f "$REPORT_DIR/junit.xml"
  target=("$TESTS_DIR/${RUN_PATHS[0]}")
elif [[ -n "$CONFIG" ]]; then
  target=(--config "$CONFIG" "$TESTS_DIR")
else
  {
    echo "flows:"
    printf '  - "%s"\n' "${RUN_PATHS[@]}"
    echo "executionOrder:"
    echo "  continueOnFailure: true"
  } > "$RUN_CONFIG"
  target=(--config "$RUN_CONFIG" "$TESTS_DIR")
fi

# Maestro only prints a flow when it finishes. Its log says when each flow starts, so follow it to show
# which flow is running and how many are left. Not needed for a single file, which shows every step.
DEBUG_LOG="$REPORT_DIR/debug/maestro.log"
mkdir -p "$(dirname "$DEBUG_LOG")"
mv -f "$DEBUG_LOG" "$DEBUG_LOG.previous" 2>/dev/null || true
PROGRESS_PID=""
if (( ${#report_args[@]} > 0 )); then
  (
    started=0
    while sleep 1; do
      [[ -f "$DEBUG_LOG" ]] || continue
      count="$(grep -c 'Running flow ' "$DEBUG_LOG" || true)"
      while (( started < count )); do
        started=$((started + 1))
        name="$(grep 'Running flow ' "$DEBUG_LOG" | sed -n "${started}p" | sed 's/.*Running flow  *//')"
        echo "▶ [$started/$TOTAL_FLOWS] $name"
      done
    done
  ) &
  PROGRESS_PID=$!
  disown
fi

echo "Running $TOTAL_FLOWS flow(s)..."
status=0
maestro --device "$DEVICE" test \
  -e APP_ID="$APP_ID" \
  -e EXPECTED_HOST="$EXPECTED_HOST" \
  -e MOCK_PORT="$MOCK_PORT" \
  -e MAP_MODE="$MAP_MODE" \
  ${report_args[@]+"${report_args[@]}"} \
  --test-output-dir "$REPORT_DIR/artifacts" \
  --debug-output "$(dirname "$DEBUG_LOG")" --flatten-debug-output \
  ${MAESTRO_ARGS[@]+"${MAESTRO_ARGS[@]}"} \
  "${target[@]}" || status=$?
[[ -n "$PROGRESS_PID" ]] && kill "$PROGRESS_PID" 2>/dev/null || true

# Write a workspace config with the flows that failed, so they can be retried without running everything again.
# The junit "file" attribute is relative to the directory maestro ran from; config paths are relative to TESTS_DIR.
# A single file run has no JUnit report and leaves the previous config alone.
[[ -f "$REPORT_DIR/junit.xml" ]] || exit "$status"
failed_flows=()
while IFS= read -r file; do
  [[ "$file" = /* ]] || file="$PWD/$file"
  file="$(cd "$(dirname "$file")" && pwd)/$(basename "$file")"
  failed_flows+=("${file#"$TESTS_DIR"/}")
done < <(grep '<testcase ' "$REPORT_DIR/junit.xml" | grep -v 'status="SUCCESS"' | sed -n 's/.* file="\([^"]*\)".*/\1/p')

if (( ${#failed_flows[@]} > 0 )); then
  {
    echo "flows:"
    printf '  - "%s"\n' "${failed_flows[@]}"
    echo "executionOrder:"
    echo "  continueOnFailure: true"
  } > "$FAILED_CONFIG"
  echo
  echo "${#failed_flows[@]} flow(s) failed. Retry them with:"
  echo "  $0 retry"
else
  rm -f "$FAILED_CONFIG"
fi

exit "$status"
