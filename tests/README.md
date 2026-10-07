# SeviBus UI tests (Maestro)

Black-box tests that drive the SeviBus Android app through its UI. They are split in two suites:

- **`integration/`**: each case covers one feature, with the app against a local [WireMock](https://wiremock.org) that answers every request. Deterministic and fast, and no request reaches a real backend.
- **`e2e/`**: a separate smoke suite against the real dev backend, with its own cases (`E2E-nn`). It only checks that the app is not completely broken against the real server: it reaches the backend, downloads transit data, and loads live arrivals.

## Running

Prerequisites:
- An Android emulator with Google Play services, or a real device. The device language must be **Spanish (Spain, `es-ES`)**, the app's default language. `run.sh` refuses to run with any other locale. See [Emulator](#emulator) for the recommended setup.
- A real device must be awake and unlocked when the run starts.
- The build under test installed (`com.sloy.sevibus.debug` by default).
- Maestro CLI 2.x (`maestro --version`).
- JDK 21 on `PATH`. WireMock is a jar downloaded on first use into `build/` (SHA-256 checked) and started by `run.sh` for integration flows.
- Optional: [fzf](https://github.com/junegunn/fzf) for the interactive flow picker. Without it, `run.sh` shows a numbered list.
- No Maestro MCP server connected. It holds host port 7001 and the CLI hangs (`DEADLINE_EXCEEDED`). Disconnect it in `/mcp` first.

### Emulator

Use a **Google APIs** image without the Play Store, API 30, x86_64 (on Apple Silicon, arm64-v8a), with 4 GB of RAM:

```bash
sdkmanager "system-images;android-30;google_apis;x86_64"
avdmanager create avd -n E2E_API_30 -k "system-images;android-30;google_apis;x86_64" -d pixel_5
# In ~/.android/avd/E2E_API_30.avd/config.ini: hw.ramSize=4096, hw.gpu.enabled=yes, hw.keyboard=yes
emulator -avd E2E_API_30 -no-snapshot-save -no-boot-anim
```

Google APIs images allow `adb root`, so the language is set without the UI:

```bash
adb root
adb shell 'setprop persist.sys.locale es-ES && setprop ctl.restart zygote'
```

Why this image:
- Play Store images run Google apps and updates in the background. The emulator stays at a load average of 9 to 15 during a run, and flows stall waiting for the app.
- ATD images (`google_atd`) don't render: screenshots are black, which breaks Maestro and the map.
- With 2 GB of RAM the app gets killed and flows stall on a blank screen.

On a Play Store image, `adb root` is refused. Set the language from Settings → System → Languages & input → Languages → Add a language → Español (España), then drag it above English.

### What `run.sh` does to the device

Before starting, `run.sh` checks the locale and the screen, and prepares the device:

- It wakes the device and stops if it is locked. A pattern or PIN can't be bypassed, unlock it first.
- It extends the screen timeout for the whole run, because wireless adb can't use `svc power stayon`. If the run is killed, the script printed the command to restore it at the start.
- It disables window, transition and animator animations. Maestro waits for the screen to settle before and after every tap, and animations delay that. Maestro's own `disableAnimations` option only works on Maestro Cloud.
- It compiles the app ahead of time (`cmd package compile -m speed`). A fresh install runs interpreted, and every cold start takes about 20s on an emulator. It takes about 30s after an install and nothing afterwards.

The screen timeout and the animation scales are restored when the run ends.

The app is also launched with the home map disabled (see `MAP_MODE`). Drawing the stop markers keeps an emulator busy, and with the map on, searches and scrolls time out. No flow uses the map.

```bash
./scripts/run.sh                                  # interactive: pick a suite, integration areas, single flows, or retry
./scripts/run.sh integration                      # a whole suite: integration, e2e or all
./scripts/run.sh retry                            # run again the flows that failed in the last run
./scripts/run.sh integration/search integration/cards e2e   # some folders or flows, from any suite
./scripts/run.sh integration/lines/lines-03-switch-direction.yaml   # single file: live step-by-step output, no JUnit report
./scripts/run.sh --config my-config.yaml          # the flows of a Maestro workspace config
./scripts/run.sh integration -- --include-tags level1               # arguments after -- go to maestro test
```

The interactive mode uses fzf menus: first the suite, then, for integration, all of it or some areas (`CARDS`, `FAV`, `NEAR`, …). It also offers to retry the failed flows of the last run and to pick single flows.

How a run works:
- `run.sh` turns the selection into a Maestro workspace config, `build/reports/run-config.yaml`, with one entry per flow or folder (`integration/cards/**`), and runs `maestro test --config` on it. A single flow file runs on its own, with Maestro's step-by-step output.
- Maestro only prints a flow when it finishes. `run.sh` follows Maestro's log (`build/reports/debug/maestro.log`) to print each flow as it starts, with how many are left: `▶ [3/34] CARDS-02 ...`.
- After a run with failures, it writes `build/reports/failed-config.yaml`, which `run.sh retry` runs.
- Integration flows start WireMock. E2E flows first send a request to wake up the dev backend, which scales to zero after a long idle period and takes a few seconds to start. The script doesn't wait for the answer, it keeps preparing the device in the meantime.

Environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `APP_ID` | `com.sloy.sevibus.debug` | Package under test |
| `EXPECTED_HOST` | `appdev-vd4mgiw7ma-no.a.run.app` | Dev backend host: woken up before e2e runs, and the host E2E-01 expects |
| `DEVICE` | first `adb` device | Target device serial |
| `MOCK_PORT` | `8089` | WireMock port for integration flows. An existing WireMock on it is reused, any other listener makes `run.sh` pick the next free port |
| `MAP_MODE` | `disabled` | `debugMapMode` launch argument. `disabled` keeps the home map out of the composition, `full` shows it |
| `REPORT_DIR` | `build/reports` | JUnit report (`junit.xml`), failure screenshots, `wiremock.log` and `wiremock-requests.json` |

### E2E: the dev backend

E2E flows run against the public dev endpoint (Cloud Run, Madrid, `dev-db`), which is the debug build's default. They start with `subflows/launch-dev.yaml`. The Tussam API only accepts Spanish IPs, so a locally run backend can't serve live data from outside Spain.

Keep this suite small and basic: it answers "is the app completely broken against the real server?". Features are covered in integration.

### Integration: mocked responses

Every request of an integration flow is answered by WireMock. `run.sh` starts it and runs `adb reverse` for its port, and flows select responses from YAML.

How it works:

```
flow ── output.mocks.set(route, variant) ──▶ WireMock admin API (scenario state)
app  ── debugApiHost launch argument ─────▶ debug-menu host override ──▶ WireMock
WireMock: stub for the selected variant, otherwise the route's default (mappings/defaults.json)
```

1. `run.sh` starts a pinned WireMock jar on `MOCK_PORT` with `mocks/` as its root and forwards the port to the device.
2. `launch-mocked.yaml` resets every scenario and launches the debug app with the `debugApiHost` argument, so all API calls go to WireMock.
3. `mappings/defaults.json` answers every endpoint the app calls: lines, stops, routes and paths recorded from dev, a health check, fixed arrivals for stop 844 and an empty list for other stops, and "card not found" for unknown cards.
4. Routes with variants are scenarios. Setting a variant (`output.mocks.set('arrivals-844', 'error')`) switches that route's response, so a flow only describes what it needs to control.
5. The variant can change mid-flow (STOP-05 goes from `error` to `fixed`) or between app restarts (SYNC-01, CARDS-05) to simulate server-side changes.
6. A request with no mock gets a 404 and fails the flow. `run.sh` lists those requests at the end of the run.

Available helpers (`mocks/api.js`): `output.mocks.set(route, variant)`, `output.mocks.setAll("route:variant,route:variant")` and `output.mocks.reset()`.

```
mocks/api.js           Maestro control layer (output.mocks)
mocks/mappings/        defaults.json (one response per endpoint) and one file per route with variants
mocks/__files/         response bodies recorded from dev
mocks/proxy-dev.json   catch-all proxy to dev, only installed while recording
subflows/launch-mocked.yaml   fresh launch pointed at the mock
scripts/record.sh      records responses from dev as a starting point for fixtures
```

- A route is a WireMock scenario named `<endpoint>-<id>` (`arrivals-844`, `card-123456789012`, `stops`) and each variant is a scenario state (`fixed`, `empty`, `error`, `low`, `recovered`, `original`, `renamed`, `added`). The initial state `Started` matches no variant, so the request gets the default from `defaults.json`.
- An integration flow starts with `subflows/launch-mocked.yaml` and picks variants with `- evalScript: ${ output.mocks.set('arrivals-844', 'fixed') }`. Use `output.mocks`, not a bare `mocks`: values from `runScript` are only shared through `output`. Pass `env: LOCATION: deny` to launch without the location permission.
- Responses requested at startup (lines, routes, stops) must be chosen before the launch with `env: MOCKS: "stops:original,lines:added"` on `launch-mocked.yaml`.
- A wrong route or variant fails the `evalScript` step with WireMock's message (`404 ... does not exist`, `422 ... does not support state`).
- The app is pointed at the mock by the `debugApiHost` launch argument, which only exists in debug builds (`DebugLaunchArguments`). It writes the debug menu's host override. Install a current debug build (`./gradlew :app:installDebug`): an older install ignores the argument and talks to dev.
- `run.sh` clears the override when the run ends, so the app is not left pointing at a stopped WireMock.
- Recording: with WireMock running, `scripts/record.sh proxy` sends every request to dev until WireMock restarts. Drive the app against it, then `scripts/record.sh <name> '<urlPattern>'` writes `build/recordings/<name>.json`. Trim it, add `scenarioName`/`requiredScenarioState`, **anonymize personal data** (card serials, balances, trip history) and move it into `mocks/`. Never commit a real card serial.
- Debugging: `build/reports/wiremock-requests.json` lists what the app requested and `wiremock.log` is the server log.
- Limits: one WireMock holds one global state, so integration flows must not be sharded across devices.
- Recorded data is marked as generated in `.gitattributes`, so GitHub collapses it in diffs.

## Layout

```
scripts/run.sh             suite selection, preflight checks, WireMock lifecycle, maestro test with JUnit output
scripts/wiremock.sh        WireMock download, port selection, start/stop and unmatched request report
scripts/record.sh          records responses from dev for fixtures
mocks/                     WireMock mappings, bodies and api.js
subflows/                  reusable steps (launches, search, location presets)
integration/<category>/    mocked flows, one folder per feature, one file per test case
e2e/                       smoke flows against the dev backend
```

Conventions:
- One test case per file, named `<prefix>-<nn>-<slug>.yaml`, with `name: "<ID> <description>"`.
- Tags: the category and the level (`level1`, …).
- Every flow starts from a clean state: `subflows/launch-mocked.yaml` in integration, `subflows/launch-dev.yaml` in e2e. Both clear app data and grant location, and debug builds never show the in-app review dialog.
- A flow that calls `launchApp` itself passes `debugMapMode: ${MAP_MODE}` in its `arguments`, otherwise the map comes back after a restart.
- Select by visible copy, in Spanish as in `app/src/main/res/values/strings.xml`. Use regex for dynamic parts (`"Parada \\d+"`, `".*3003"`).

### Maestro gotchas

- `inputText` can't type non-ASCII characters on Android ("Unknown error"). Search with ASCII queries; the app ignores accents.
- `hideKeyboard` sends Back on Android and leaves full-screen destinations (Travel Card, Search). Don't use it.
- `clearState` resets the per-app locale, which is why the suite relies on the device language.
- Some labels are hardcoded in the app code and don't come from `strings.xml` (`Profile`, `Close screen`, `Clear search`, `N min`). They stay as they are.
- Never turn airplane mode on from a flow. A real device is usually connected through wireless adb and the connection drops. Simulate offline by relaunching with `debugApiHost: "http://localhost:9"` (see CONN-01).
- Amounts use a non-breaking space before `€`. Match them with `"."` (`"12,50.€"`), a plain space does not match.
- The map is disabled by default (`MAP_MODE`). With `MAP_MODE=full`, each stop marker shows up as an unlabeled node in the view hierarchy (about 600 of them). Map interactions are out of scope for now.

## Test catalog

### E2E suite (`e2e/`)

Smoke cases against the dev backend.

| ID | Case | Status |
|---|---|---|
| E2E-01 | The app reaches the dev backend and its health check reports the dev host | ✅ |
| E2E-02 | Transit data downloads from dev: Lines lists the line groups and a line opens its route | ✅ |
| E2E-03 | Searching a stop opens its detail and loads live arrivals without errors. Fails now and then until SEVAND-10 is fixed | ✅ |

### Integration suite (`integration/`)

One table per area. Status: ✅ implemented and passing · ⏳ pending · 🚫 blocked (needs app or server support)

#### Navigation (`integration/navigation/`)

| ID | Level | Case | Status |
|---|---|---|---|
| NAV-02 | 1 | Bottom navigation switches between For You, Lines and Travel Card | ✅ |
| NAV-03 | 1 | Back unwinds stop and line screens back to home | ✅ |

#### Nearby (`integration/nearby/`)

| ID | Level | Case | Status |
|---|---|---|---|
| NEAR-01 | 1 | Nearby lists closest stops with distance at Plaza Nueva | ✅ |
| NEAR-02 | 1 | Nearby empty state outside Seville | ✅ |
| NEAR-03 | 1 | Nearby asks to activate location without permission | ✅ |
| NEAR-04 | 1 | Tapping a nearby stop opens its detail | ✅ |
| NEAR-05 | 1 | Granting permission from "Activate location" loads nearby stops | ⏳ |

#### Search (`integration/search/`)

| ID | Level | Case | Status |
|---|---|---|---|
| SEARCH-01 | 1 | Line name search opens the line route | ✅ |
| SEARCH-02 | 1 | Stop name search opens the stop detail | ✅ |
| SEARCH-03 | 1 | Accent-insensitive and prefix matching | ✅ |
| SEARCH-04 | 1 | Clear query empties results, close returns home | ✅ |
| SEARCH-05 | 1 | Query with no results shows an empty state | ⏳ |

#### Lines (`integration/lines/`)

| ID | Level | Case | Status |
|---|---|---|---|
| LINES-01 | 1 | Lines tab lists lines grouped and scrolls to the end | ✅ |
| LINES-02 | 1 | Opening a line shows origin, destination and stops | ✅ |
| LINES-03 | 1 | Switching direction shows the other route's stops | ✅ |
| LINES-04 | 1 | Tapping a stop in a route opens the stop detail | ✅ |

#### Stop detail (`integration/stop-detail/`)

| ID | Level | Case | Status |
|---|---|---|---|
| STOP-01 | 1 | Header with name, code, line badges and favorite action | ✅ |
| STOP-02 | 1 | Opening stop 844 shows the arrival times of the fixture (2, 5, 10 and 15 min) | ✅ |
| STOP-03 | 1 | Tapping the Rochelambert arrival opens the line route with the stop highlighted | ✅ |
| STOP-04 | 3 | Stop with no buses (empty arrivals list) shows "No disponible" for every line | ✅ |
| STOP-05 | 3 | Arrivals error, then recovery on the next 20 s poll once the server answers again | ✅ |
| STOP-06 | 3 | An arrival flagged as the last bus shows the "Last bus" badge | ✅ |

#### Favorites (`integration/favorites/`)

| ID | Level | Case | Status |
|---|---|---|---|
| FAV-01 | 1 | Logged out: Favorites tab invites to sign in | ✅ |
| FAV-02 | 1 | Logged out: saving a favorite asks to sign in | ✅ |
| FAV-03 | 2 | Login turns the empty state into the favorites list; logout clears it | 🚫 needs e2e login |
| FAV-04 | 2 | Add a favorite from stop detail; it is listed in For You and stored on the server | 🚫 needs e2e login |
| FAV-05 | 2 | Edit favorites: rename, icon, reorder, delete; survives reinstall + login | 🚫 needs e2e login |
| FAV-06 | 3 | Favorite added on the server appears after re-login | 🚫 needs login + server test API |
| FAV-07 | 3 | Favorite deleted on the server is removed locally | 🚫 needs login + server test API |
| FAV-08 | 3 | Two devices editing at once: last-write-wins behaviour | 🚫 needs login + server test API |
| FAV-09 | 3 | Edit while offline: local save, then reconciliation | 🚫 needs e2e login |

#### Travel cards (`integration/cards/`)

| ID | Level | Case | Status |
|---|---|---|---|
| CARDS-01 | 1 | Add card form shown when no cards are saved | ✅ |
| CARDS-02 | 1 | Unknown 12-digit serial shows a not found error | ✅ |
| CARDS-03 | 1 | Help screen opens and closes | ✅ |
| CARDS-04 | 2 | Add a valid card: balance and recent activity; delete it. Rename does not exist in the app yet and reorder is not covered | ✅ |
| CARDS-05 | 2 | Low balance alert: tapping the card opens the detail with the trips chip and top-up button, the dismiss button hides it, it returns after the balance recovers and drops again (card balances refresh on app start) | ✅ |
| CARDS-06 | 2 | Cards sync to the server on login and survive logout | 🚫 needs e2e login |
| CARDS-07 | 1 | Serial with leading zeros finds the card. The app sends it as a number (`/api/card/0`), which the backend also does with `parseInt`, so the mock matches any number of zeros | ✅ |

#### Settings (`integration/settings/`)

| ID | Level | Case | Status |
|---|---|---|---|
| SET-01 | 1 | Settings opens from Profile and closes | ✅ |
| SET-02 | 1 | Dark mode preference survives restart | ✅ |
| SET-03 | 1 | Analytics opt-out survives restart | ✅ |

#### Connectivity (`integration/connectivity/`)

| ID | Level | Case | Status |
|---|---|---|---|
| CONN-01 | 1 | Offline: cached data searchable, arrivals show a connection error | ✅ |
| CONN-02 | 3 | Server returns 426: app asks to upgrade (a WireMock variant can return it) | ⏳ |

#### Transit data sync (`integration/transit-sync/`)

| ID | Level | Case | Status |
|---|---|---|---|
| SYNC-01 | 3 | Stop renamed on the server is reflected after restart, including search | ✅ |
| SYNC-02 | 3 | New line on the server appears in Lines and search after restart | ✅ |

#### Map (out of scope for now)

Stop markers, bus markers and polylines aren't reachable through the accessibility tree.

## Findings from the suite

Recorded while building Level 1. They need app changes, which are owned outside this suite.
- The Travel Card help button's content description is "Apply order" (`CardsScreen.kt:212`).
- Hardcoded Spanish under the English locale: "No disponible", card messages, all FAQ entries.
- The analytics switch has no accessible label; tests locate it relative to its title.
- The search box keeps the previous stop name when reopened from a stop.
- Arrivals keep polling after leaving the stop detail.
- Dev `/api/paths` item 97 has no `polyline`/`checksum`, so the app logs `MissingFieldException: Error refreshing Path local data`.
- The card screen has no rename (the code is commented out in `CardInfoElement.kt`).
- Adding a card with an unknown serial only shows a toast. There is no hint under the serial field.
- Card balances refresh only when the app process starts (`RemoteAndLocalCardsRepository` init), not when the Travel Card or For You screen opens.
- The first sync after a clean launch can still be running when a flow searches; `search.yaml` right after launch is occasionally empty on a slow device.
- Searching right after a fresh install can crash in `LineElement.kt:26`: `searchLines()` reads stored routes without waiting for their download, so lines come back without routes (SEVAND-10).
