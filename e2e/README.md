# SeviBus end-to-end tests (Maestro)

Black-box tests that drive the SeviBus Android app against a real backend. They cover the app and the server together. Flows tagged `mocks` go through a local WireMock that serves chosen responses and proxies everything else to the dev backend.

## Running

Prerequisites:
- An Android emulator with Google Play services (e.g. `Medium_Phone`, API 36 `google_apis`). The device language must be **English**.
- The build under test installed (`com.sloy.sevibus.debug` by default).
- Maestro CLI 2.x (`maestro --version`).
- JDK 21 on `PATH`. WireMock is a jar downloaded on first use into `build/` (SHA-256 checked) and started by `run.sh`.
- No Maestro MCP server connected. It holds host port 7001 and the CLI hangs (`DEADLINE_EXCEEDED`). Disconnect it in `/mcp` first.

```bash
./scripts/run.sh                                  # whole suite (config.yaml)
./scripts/run.sh flows/search                     # one category
./scripts/run.sh flows/lines/lines-03-switch-direction.yaml   # single file: live step-by-step output, no JUnit report
./scripts/run.sh flows --include-tags level1      # extra Maestro args go after the target
```

Environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `APP_ID` | `com.sloy.sevibus.debug` | Package under test |
| `EXPECTED_HOST` | `appdev-vd4mgiw7ma-no.a.run.app` | Backend host the app must report (SET-04) |
| `DEVICE` | first `adb` device | Target device serial |
| `MOCK_PORT` | `8089` | WireMock port. An existing WireMock on it is reused, any other listener makes `run.sh` pick the next free port |
| `REPORT_DIR` | `build/reports` | JUnit report (`junit.xml`), failure screenshots, `wiremock.log` and `wiremock-requests.json` |

### Backend

Level 1 runs against the public dev endpoint (Cloud Run, Madrid, `dev-db`), which is the debug build's default. The Tussam API only accepts Spanish IPs, so a locally run backend can't serve live data from outside Spain. The plan for a local server (a Tussam proxy hosted in Madrid plus a local SeviBus API) is pending.

### Mocked responses

Cases that need specific data (fixed arrival times, errors, empty lists, a server-side change) use [WireMock](https://wiremock.org). `run.sh` starts it and runs `adb reverse` for its port, and flows select the response from YAML.

How it works:

```
flow ── output.mocks.set(route, variant) ──▶ WireMock admin API (scenario state)
app  ── debugApiHost launch argument ─────▶ debug-menu host override ──▶ WireMock
WireMock: stub for the selected variant, otherwise proxy to the dev backend
```

1. `run.sh` starts a pinned WireMock jar on `MOCK_PORT` with `e2e/mocks` as its root and forwards the port to the device.
2. `launch-mocked.yaml` resets every scenario and launches the debug app with the `debugApiHost` argument, so all API calls go to WireMock instead of dev.
3. Every route that has fixtures is a scenario. Setting a variant (`output.mocks.set('arrivals-844', 'error')`) switches that route's response. Routes without a selected variant, and routes without fixtures, are proxied to dev, so a mocked flow only describes what it needs to control.
4. The variant can change mid-flow (STOP-05 goes from `error` to `fixed`) or between app restarts (SYNC-01, CARDS-05) to simulate server-side changes.

Available helpers (`mocks/api.js`): `output.mocks.set(route, variant)`, `output.mocks.setAll("route:variant,route:variant")` and `output.mocks.reset()`.

```
mocks/api.js           Maestro control layer (output.mocks)
mocks/mappings/        one file per route: _proxy-dev.json (catch-all to dev) and a scenario per route
mocks/__files/         response bodies recorded from dev
subflows/launch-mocked.yaml   fresh launch pointed at the mock
scripts/record.sh      saves proxied responses as a starting point for fixtures
```

- A route is a WireMock scenario named `<endpoint>-<id>` (`arrivals-844`, `card-123456789012`, `stops`) and each variant is a scenario state (`fixed`, `empty`, `error`, `low`, `recovered`, `original`, `renamed`, `added`). The initial state `Started` matches no stub, so the request goes to dev.
- A mocked flow starts with `subflows/launch-mocked.yaml`, carries the `mocks` tag and picks variants with `- evalScript: ${ output.mocks.set('arrivals-844', 'fixed') }`. Use `output.mocks`, not a bare `mocks`: values from `runScript` are only shared through `output`.
- Responses requested at startup (lines, routes, stops) must be chosen before the launch with `env: MOCKS: "stops:original,lines:added"` on `launch-mocked.yaml`.
- A wrong route or variant fails the `evalScript` step with WireMock's message (`404 ... does not exist`, `422 ... does not support state`), so a typo can't silently reach dev.
- The app is pointed at the mock by the `debugApiHost` launch argument, which only exists in debug builds (`DebugLaunchArguments`). It writes the debug menu's host override. Install a current debug build (`./gradlew :app:installDebug`): an older install ignores the argument and every mocked flow talks to dev.
- `run.sh` clears the override when the run ends, so the app is not left pointing at a stopped WireMock.
- Recording: start WireMock, drive the app against it, then `scripts/record.sh <name> '<urlPattern>'` writes `build/recordings/<name>.json`. Trim it, add `scenarioName`/`requiredScenarioState`, **anonymize personal data** (card serials, balances, trip history) and move it into `mocks/`. Never commit a real card serial.
- Debugging: `build/reports/wiremock-requests.json` lists what the app requested and `wiremock.log` is the server log.
- Limits: one WireMock holds one global state, so mocked flows must not be sharded across devices. A flow that restarts the app without `clearState` must serve both versions of a list from stubs, because lists served by dev carry `Cache-Control: max-age=21600`.

## Layout

```
config.yaml            suite definition (flows/**)
scripts/run.sh         preflight checks, WireMock lifecycle, maestro test with JUnit output
scripts/wiremock.sh    WireMock download, port selection and start/stop helpers
scripts/record.sh      records proxied responses for fixtures
mocks/                 WireMock mappings, bodies and api.js
subflows/              reusable steps (fresh launch, search, location presets)
flows/<category>/      one folder per feature, one file per test case
```

Conventions:
- One test case per file, named `<prefix>-<nn>-<slug>.yaml`, with `name: "<ID> <description>"`.
- Tags: the category, the level (`level1`, …), `live-data` when the flow depends on real-time Tussam data and `mocks` when it uses WireMock.
- Every flow starts from a clean state with `subflows/launch-fresh.yaml` (clears app data, grants location, dismisses the debug in-app review dialog), or `subflows/launch-mocked.yaml` for mocked flows.
- Select by visible copy. Use regex for dynamic parts (`"Stop \\d+"`, `".*3003"`).

### Maestro gotchas

- `inputText` can't type non-ASCII characters on Android ("Unknown error"). Search with ASCII queries; the app ignores accents.
- `hideKeyboard` sends Back on Android and leaves full-screen destinations (Travel Card, Search). Don't use it.
- `clearState` resets the per-app locale, which is why the suite relies on the device language.
- Amounts use a non-breaking space before `€`. Match them with `"."` (`"12,50.€"`), a plain space does not match.
- Map markers are invisible to Maestro (Google Maps). Map interactions are out of scope for now.

## Test catalog

Status: ✅ implemented and passing · ⏳ pending · 🚫 blocked (needs app or server support)

### Navigation (`flows/navigation`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| NAV-01 | 1 | dev | Cold start shows For You with tabs and bottom navigation | ✅ |
| NAV-02 | 1 | dev | Bottom navigation switches between For You, Lines and Travel Card | ✅ |
| NAV-03 | 1 | dev | Back unwinds stop and line screens back to home | ✅ |

### Nearby (`flows/nearby`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| NEAR-01 | 1 | dev | Nearby lists closest stops with distance at Plaza Nueva | ✅ |
| NEAR-02 | 1 | dev | Nearby empty state outside Seville | ✅ |
| NEAR-03 | 1 | dev | Nearby asks to activate location without permission | ✅ |
| NEAR-04 | 1 | dev | Tapping a nearby stop opens its detail | ✅ |
| NEAR-05 | 1 | dev | Granting permission from "Activate location" loads nearby stops | ⏳ |

### Search (`flows/search`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| SEARCH-01 | 1 | dev | Line name search opens the line route | ✅ |
| SEARCH-02 | 1 | dev | Stop name search opens the stop detail | ✅ |
| SEARCH-03 | 1 | dev | Accent-insensitive and prefix matching | ✅ |
| SEARCH-04 | 1 | dev | Clear query empties results, close returns home | ✅ |
| SEARCH-05 | 1 | dev | Query with no results shows an empty state | ⏳ |

### Lines (`flows/lines`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| LINES-01 | 1 | dev | Lines tab lists lines grouped and scrolls to the end | ✅ |
| LINES-02 | 1 | dev | Opening a line shows origin, destination and stops | ✅ |
| LINES-03 | 1 | dev | Switching direction shows the other route's stops | ✅ |
| LINES-04 | 1 | dev | Tapping a stop in a route opens the stop detail | ✅ |

### Stop detail (`flows/stop-detail`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| STOP-01 | 1 | dev | Header with name, code, line badges and favorite action | ✅ |
| STOP-02 | 1 | mock | Opening stop 844 shows the arrival times of the fixture (2, 5, 10 and 15 min) | ✅ |
| STOP-03 | 1 | mock | Tapping the Rochelambert arrival opens the line route with the stop highlighted | ✅ |
| STOP-04 | 3 | mock | Stop with no buses shows a "no service" message instead of loading placeholders. Fails until the app has such a message (the skeleton never ends) | ⏳ |
| STOP-05 | 3 | mock | Arrivals error, then recovery on the next 20 s poll once the server answers again | ✅ |
| STOP-06 | 3 | mock | An arrival flagged as the last bus shows the "Last bus" badge | ✅ |

### Favorites (`flows/favorites`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| FAV-01 | 1 | dev | Logged out: Favorites tab invites to sign in | ✅ |
| FAV-02 | 1 | dev | Logged out: saving a favorite asks to sign in | ✅ |
| FAV-03 | 2 | dev | Login turns the empty state into the favorites list; logout clears it | 🚫 needs e2e login |
| FAV-04 | 2 | dev | Add a favorite from stop detail; it is listed in For You and stored on the server | 🚫 needs e2e login |
| FAV-05 | 2 | dev | Edit favorites: rename, icon, reorder, delete; survives reinstall + login | 🚫 needs e2e login |
| FAV-06 | 3 | dev | Favorite added on the server appears after re-login | 🚫 needs login + server test API |
| FAV-07 | 3 | dev | Favorite deleted on the server is removed locally | 🚫 needs login + server test API |
| FAV-08 | 3 | dev | Two devices editing at once: last-write-wins behaviour | 🚫 needs login + server test API |
| FAV-09 | 3 | dev | Edit while offline: local save, then reconciliation | 🚫 needs e2e login |

### Travel cards (`flows/cards`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| CARDS-01 | 1 | dev | Add card form shown when no cards are saved | ✅ |
| CARDS-02 | 1 | dev | Unknown 12-digit serial shows a not found error | ✅ |
| CARDS-03 | 1 | dev | Help screen opens and closes | ✅ |
| CARDS-04 | 2 | mock | Add a valid card: balance and recent activity; delete it. Rename does not exist in the app yet and reorder is not covered | ✅ |
| CARDS-05 | 2 | mock | Low balance alert: "View" opens the card, "Dismiss" hides it, it returns after the balance recovers and drops again (card balances refresh on app start) | ✅ |
| CARDS-06 | 2 | dev | Cards sync to the server on login and survive logout | 🚫 needs e2e login |
| CARDS-07 | 1 | mock | Serial with leading zeros is sent intact (currently `000…` becomes `/api/card/0`). Fails until the app is fixed | ⏳ |

### Settings (`flows/settings`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| SET-01 | 1 | dev | Settings opens from Profile and closes | ✅ |
| SET-02 | 1 | dev | Dark mode preference survives restart | ✅ |
| SET-03 | 1 | dev | Analytics opt-out survives restart | ✅ |
| SET-04 | 1 | dev | Server health check reports the expected host | ✅ |

### Connectivity (`flows/connectivity`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| CONN-01 | 1 | dev | Offline: cached data searchable, arrivals show a connection error | ✅ |
| CONN-02 | 3 | dev | Server returns 426: app asks to upgrade | 🚫 needs server test API |

### Transit data sync (`flows/transit-sync`)

| ID | Level | Backend | Case | Status |
|---|---|---|---|---|
| SYNC-01 | 3 | mock | Stop renamed on the server is reflected after restart, including search | ⏳ written, not verified |
| SYNC-02 | 3 | mock | New line on the server appears in Lines and search after restart | ⏳ written, not verified |

### Map (out of scope for now)

Stop markers, bus markers and polylines aren't reachable through the accessibility tree.

## Findings from the suite

Recorded while building Level 1. They need app changes, which are owned outside this suite.
- Stop with an empty arrivals list stays in the loading skeleton forever (`StopDetailViewModel.kt:62`).
- Card serials lose leading zeros (`000000000000` requests `/api/card/0`).
- The Travel Card help button's content description is "Apply order" (`CardsScreen.kt:212`).
- Hardcoded Spanish under the English locale: "Origen"/"Destino", "No disponible", card messages, all FAQ entries.
- The analytics switch has no accessible label; tests locate it relative to its title.
- The search box keeps the previous stop name when reopened from a stop.
- Arrivals keep polling after leaving the stop detail.
- Dev `/api/paths` item 97 has no `polyline`/`checksum`, so the app logs `MissingFieldException: Error refreshing Path local data`.
- The card screen has no rename (the code is commented out in `CardInfoElement.kt`).
- Adding a card with an unknown serial only shows a toast. There is no hint under the serial field.
- Card balances refresh only when the app process starts (`RemoteAndLocalCardsRepository` init), not when the Travel Card or For You screen opens.
- The first sync after a clean launch can still be running when a flow searches; `search.yaml` right after launch is occasionally empty on a slow device.
