#!/usr/bin/env bash
# WireMock helpers sourced by run.sh and record.sh.

WIREMOCK_VERSION="3.13.2"
WIREMOCK_SHA256="d097b19bd483c5038479b13a5c71e9faf8f2f5106584f0c120a7770ab0bdb367"
WIREMOCK_JAR="$TESTS_DIR/build/wiremock-standalone-$WIREMOCK_VERSION.jar"
WIREMOCK_PID=""

wiremock_port_is_wiremock() {
  curl -sf -m 2 "http://localhost:$1/__admin/health" 2>/dev/null | grep -q '"version"'
}

wiremock_port_is_free() {
  ! lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

wiremock_download() {
  [[ -f "$WIREMOCK_JAR" ]] && return 0
  command -v java >/dev/null || { echo "java not found. WireMock needs JDK 21 (same as the Android build)." >&2; return 1; }
  mkdir -p "$(dirname "$WIREMOCK_JAR")"
  local url="https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/$WIREMOCK_VERSION/wiremock-standalone-$WIREMOCK_VERSION.jar"
  echo "Downloading WireMock $WIREMOCK_VERSION..."
  curl -sfL -o "$WIREMOCK_JAR.tmp" "$url" || { echo "WireMock download failed: $url" >&2; return 1; }
  if [[ "$(shasum -a 256 "$WIREMOCK_JAR.tmp" | awk '{print $1}')" != "$WIREMOCK_SHA256" ]]; then
    rm -f "$WIREMOCK_JAR.tmp"
    echo "WireMock checksum mismatch" >&2
    return 1
  fi
  mv "$WIREMOCK_JAR.tmp" "$WIREMOCK_JAR"
}

# Reuses a WireMock already listening on MOCK_PORT, or starts one on the first free port from MOCK_PORT up.
wiremock_start() {
  local root_dir="$1" log_file="$2" port="${MOCK_PORT:-8089}"
  if wiremock_port_is_wiremock "$port"; then
    echo "Reusing WireMock on port $port"
    export MOCK_PORT="$port"
    return 0
  fi
  while ! wiremock_port_is_free "$port"; do
    port=$((port + 1))
  done
  wiremock_download || return 1
  java -jar "$WIREMOCK_JAR" --port "$port" --root-dir "$root_dir" --disable-banner >"$log_file" 2>&1 &
  WIREMOCK_PID=$!
  for _ in $(seq 1 60); do
    if wiremock_port_is_wiremock "$port"; then
      echo "WireMock started on port $port"
      export MOCK_PORT="$port"
      return 0
    fi
    sleep 0.5
  done
  echo "WireMock did not become healthy in 30 s. See $log_file" >&2
  wiremock_stop
  return 1
}

wiremock_stop() {
  [[ -n "$WIREMOCK_PID" ]] && kill "$WIREMOCK_PID" 2>/dev/null || true
  WIREMOCK_PID=""
}

wiremock_dump_requests() {
  curl -sf -m 5 "http://localhost:$MOCK_PORT/__admin/requests" -o "$1" || true
}

# Prints the requests no mapping matched, once per method and URL. Integration flows must mock every request.
wiremock_report_unmatched() {
  local unmatched
  unmatched="$(curl -sf -m 5 "http://localhost:$MOCK_PORT/__admin/requests/unmatched" \
    | python3 -c 'import json,sys; print("\n".join(sorted({r["method"]+" "+r["url"] for r in json.load(sys.stdin)["requests"]})))' 2>/dev/null || true)"
  [[ -z "$unmatched" ]] && return 0
  echo
  echo "WireMock had no mock for these requests (add one to tests/mocks):"
  echo "$unmatched" | sed 's/^/  /'
}
