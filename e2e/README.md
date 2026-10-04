# SeviBus end-to-end tests (Maestro)

Black-box tests that drive the SeviBus Android app against a real backend. They cover the app and the server together.

## Running

Prerequisites:
- An Android emulator with Google Play services (e.g. `Medium_Phone`, API 36 `google_apis`). The device language must be **English**.
- The build under test installed (`com.sloy.sevibus.debug` by default).
- Maestro CLI 2.x (`maestro --version`).
- No Maestro MCP server connected. It holds host port 7001 and the CLI hangs (`DEADLINE_EXCEEDED`). Disconnect it in `/mcp` first.

```bash
./scripts/run.sh                                  # whole suite (config.yaml)
./scripts/run.sh flows/search                     # one category
./scripts/run.sh flows/lines/lines-03-switch-direction.yaml
./scripts/run.sh flows --include-tags level1      # extra Maestro args go after the target
```

Environment variables:

| Variable | Default | Purpose |
|---|---|---|
| `APP_ID` | `com.sloy.sevibus.debug` | Package under test |
| `EXPECTED_HOST` | `appdev-vd4mgiw7ma-no.a.run.app` | Backend host the app must report (SET-04) |
| `DEVICE` | first `adb` device | Target device serial |
| `REPORT_DIR` | `build/reports` | JUnit report (`junit.xml`) and failure screenshots |

### Backend

Level 1 runs against the public dev endpoint (Cloud Run, Madrid, `dev-db`), which is the debug build's default. The Tussam API only accepts Spanish IPs, so a locally run backend can't serve live data from outside Spain. The plan for a local server (a Tussam proxy hosted in Madrid plus a local SeviBus API) is pending.

## Layout

```
config.yaml            suite definition (flows/**)
scripts/run.sh         preflight checks + maestro test with JUnit output
subflows/              reusable steps (fresh launch, search, location presets)
flows/<category>/      one folder per feature, one file per test case
```

Conventions:
- One test case per file, named `<prefix>-<nn>-<slug>.yaml`, with `name: "<ID> <description>"`.
- Tags: the category, the level (`level1`, …) and `live-data` when the flow depends on real-time Tussam data.
- Every flow starts from a clean state with `subflows/launch-fresh.yaml` (clears app data, grants location, dismisses the debug in-app review dialog).
- Select by visible copy. Use regex for dynamic parts (`"Stop \\d+"`, `".*3003"`).

### Maestro gotchas

- `inputText` can't type non-ASCII characters on Android ("Unknown error"). Search with ASCII queries; the app ignores accents.
- `hideKeyboard` sends Back on Android and leaves full-screen destinations (Travel Card, Search). Don't use it.
- `clearState` resets the per-app locale, which is why the suite relies on the device language.
- Map markers are invisible to Maestro (Google Maps). Map interactions are out of scope for now.

## Test catalog

Status: ✅ implemented and passing · ⏳ pending · 🚫 blocked (needs app or server support)

### Navigation (`flows/navigation`)

| ID | Level | Case | Status |
|---|---|---|---|
| NAV-01 | 1 | Cold start shows For You with tabs and bottom navigation | ✅ |
| NAV-02 | 1 | Bottom navigation switches between For You, Lines and Travel Card | ✅ |
| NAV-03 | 1 | Back unwinds stop and line screens back to home | ✅ |

### Nearby (`flows/nearby`)

| ID | Level | Case | Status |
|---|---|---|---|
| NEAR-01 | 1 | Nearby lists closest stops with distance at Plaza Nueva | ✅ |
| NEAR-02 | 1 | Nearby empty state outside Seville | ✅ |
| NEAR-03 | 1 | Nearby asks to activate location without permission | ✅ |
| NEAR-04 | 1 | Tapping a nearby stop opens its detail | ✅ |
| NEAR-05 | 1 | Granting permission from "Activate location" loads nearby stops | ⏳ |

### Search (`flows/search`)

| ID | Level | Case | Status |
|---|---|---|---|
| SEARCH-01 | 1 | Line name search opens the line route | ✅ |
| SEARCH-02 | 1 | Stop name search opens the stop detail | ✅ |
| SEARCH-03 | 1 | Accent-insensitive and prefix matching | ✅ |
| SEARCH-04 | 1 | Clear query empties results, close returns home | ✅ |
| SEARCH-05 | 1 | Query with no results shows an empty state | ⏳ |

### Lines (`flows/lines`)

| ID | Level | Case | Status |
|---|---|---|---|
| LINES-01 | 1 | Lines tab lists lines grouped and scrolls to the end | ✅ |
| LINES-02 | 1 | Opening a line shows origin, destination and stops | ✅ |
| LINES-03 | 1 | Switching direction shows the other route's stops | ✅ |
| LINES-04 | 1 | Tapping a stop in a route opens the stop detail | ✅ |

### Stop detail (`flows/stop-detail`)

| ID | Level | Case | Status |
|---|---|---|---|
| STOP-01 | 1 | Header with name, code, line badges and favorite action | ✅ |
| STOP-02 | 1 | Live arrivals load (`live-data`) | ✅ |
| STOP-03 | 1 | Tapping an arrival opens the line route | ✅ |
| STOP-04 | 3 | Stop with no buses shows a "no service" state (needs a fake empty arrivals response; currently the skeleton never ends) | 🚫 |
| STOP-05 | 3 | Arrivals error then recovery on the next 20 s poll | 🚫 |
| STOP-06 | 3 | Deterministic arrival times and "Last bus" badge | 🚫 |

### Favorites (`flows/favorites`)

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

### Travel cards (`flows/cards`)

| ID | Level | Case | Status |
|---|---|---|---|
| CARDS-01 | 1 | Add card form shown when no cards are saved | ✅ |
| CARDS-02 | 1 | Unknown 12-digit serial shows a not found error | ✅ |
| CARDS-03 | 1 | Help screen opens and closes | ✅ |
| CARDS-04 | 2 | Add a valid card: balance and transactions; rename, reorder, delete | 🚫 needs a stable test card (fake Tussam) |
| CARDS-05 | 2 | Low balance alert: "View" opens the card, "Dismiss" hides it until the balance recovers | 🚫 needs fake card data |
| CARDS-06 | 2 | Cards sync to the server on login and survive logout | 🚫 needs e2e login |
| CARDS-07 | 1 | Serial with leading zeros is sent intact (currently `000…` becomes `/api/card/0`) | ⏳ |

### Settings (`flows/settings`)

| ID | Level | Case | Status |
|---|---|---|---|
| SET-01 | 1 | Settings opens from Profile and closes | ✅ |
| SET-02 | 1 | Dark mode preference survives restart | ✅ |
| SET-03 | 1 | Analytics opt-out survives restart | ✅ |
| SET-04 | 1 | Server health check reports the expected host | ✅ |

### Connectivity (`flows/connectivity`)

| ID | Level | Case | Status |
|---|---|---|---|
| CONN-01 | 1 | Offline: cached data searchable, arrivals show a connection error | ✅ |
| CONN-02 | 3 | Server returns 426: app asks to upgrade | 🚫 needs server test API |

### Transit data sync (`flows/transit-sync`, not created yet)

| ID | Level | Case | Status |
|---|---|---|---|
| SYNC-01 | 3 | Stop renamed on the server is reflected after restart, including search | 🚫 needs server test API |
| SYNC-02 | 3 | New line on the server appears in Lines and search | 🚫 needs server test API |

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
