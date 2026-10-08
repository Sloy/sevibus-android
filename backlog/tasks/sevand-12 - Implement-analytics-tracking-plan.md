---
id: SEVAND-12
title: Implement analytics tracking plan
status: Done
assignee:
  - '@claude'
created_date: '2026-10-08 01:00'
updated_date: '2026-10-08 14:50'
labels: []
dependencies: []
type: enhancement
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Implement the analytics tracking plan in `backlog/docs/doc-2 - Analytics-tracking-plan.md` in a single release: 24 new events, 13 changed events, 8 Amplitude user properties, and the tracking quirk fixes listed below. The doc is the source of truth for event names, property names, types and enum values. Do not invent names: if something in the doc is impossible or ambiguous in code, stop and leave a note in Implementation Notes instead of improvising.

Why: today we cannot tell where a stop was opened from (map taps and line-route taps fire no event), who has favorites or cards (no user properties), whether arrivals actually loaded, or how the login flow performs. Several existing events also fire more often than they should, which skews charts.

Amplitude (project SeviBus Prod) already has every new event and property in its tracking plan as "planned". Anything sent with a different name will show up there as unexpected, so names must match exactly.

Product context and dashboards: "SeviBus — Amplitude analytics plan" doc (Claude Docs).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Every event and property in doc-2 marked New or Changed is implemented with the exact name, type and enum values from the doc
- [ ] #2 The 8 user properties are set with `identify()` whenever their value changes, from both AmplitudeTracker and (as user properties) FirebaseTracker
- [ ] #3 Tracking fixes F1 to F6 below are done
- [x] #4 Unit tests cover: Stop Details Viewed `source` for each entry point, `trigger` for launch/navigation/back, Line Stops Viewed not re-firing on route switch, Arrivals Displayed firing once per screen view (not per poll), Session Summary `sessionType` classification rules, Edit Favorites Saved diff counts, Card Check Completed results
- [ ] #5 Manual check on a debug build: each new event appears in the Amplitude dev project (SeviBus Dev) with the right properties; result written in Implementation Notes
- [x] #6 doc-2 and CLAUDE.md updated if anything changed during implementation
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
### Tracking fixes (existing bugs that skew data)

- F1. `App Started` fires from `LaunchedEffect(Unit)` in `App()`, so it fires again on activity recreation (theme change, rotation, process restore). Move it to fire once per process (e.g. `SevApplication.onCreate`, or a process-level flag).
- F2. Screen views are tracked by collecting `SevNavigator.destination` in `App.kt`. That also tracks the start destination on every launch and every back navigation as if they were new visits. Add `trigger` (launch, navigation, back) to every screen view event. Suggested: `SevNavigator` exposes the last transition type alongside the destination (e.g. a `NavigationTransition(destination, trigger)` flow), set to `back` in `navigateBack()` and `launch` for the initial value.
- F3. `Line Stops Viewed` re-fires when only `routeId` or `highlightedStop` changes (route tab switch reuses the same class and `navigate()` re-emits). Only fire when `lineId` changes or the previous destination was a different screen. Route tab switches fire `Route Direction Switched` instead.
- F4. Logout calls `amplitude.reset()` in `AmplitudeTracker.monitorUserSession()`, which regenerates the Device ID, so the same person shows up as a new user after logging out. Replace with `setUserId(null)` and keep the Device ID. Same for Firebase (already `setUserId(null)`).
- F5. Property renames, sent only under the new name: `Line List Clicked.line` to `lineLabel`, `Card Top Up Clicked.type` to `cardType`, `Review Dialog Dismissed.duration` to `durationSeconds`.
- F6. `Card Scanned` fires before the lookup and there is no outcome event, so we can't tell found / not found / already saved / error apart (prod: 234 users scanned, 85 added a card in 90 days). Add `Card Check Completed` with `result` in every branch of `CardViewModel.onNewCardNumber` / `onNewCardReceived`.

### Stop Details Viewed `source`

Add `source` to `NavigationDestination.StopDetail` (default `other`, `@Serializable` field with default) and set it at each call site:
- favorites: `ForYouScreen` favorites widget
- nearby: `ForYouScreen` nearby widget (ForYouScreen's `onStopClicked` is shared; split it or pass the source)
- map: `MapContainer.onStopSelected` in `MapBottomSheetScaffold.kt`
- search: `SearchScreen`
- line_route: `LineRouteScreen.onStopClick` in `App.kt`
- back: set by the navigator when `trigger` = back (do not store it in the destination)
`highlightedLineId` = `StopDetail.highlightedLine`. Keep the existing click events (`Favorite Stop Clicked`, `Nearby Stop Clicked`, `Search Result Stop Clicked`) and add `Map Stop Clicked`, `Line Route Stop Clicked`, `Arrival Clicked`.

### Arrivals

- `Arrivals Displayed`: in `StopDetailViewModel`, the first successful emission of the arrivals loop per ViewModel instance (screen = stop_detail, `latencyMs` from screen open to first success). In the favorites and nearby widgets: first time all visible items have loaded arrivals per For You view (screen = favorites / nearby, `stopCount`, `arrivalsCount` = total arrivals shown). Never on the 20 s polls.
- `Arrivals Failed`: every failed request, polls included, with `errorType` mapped from the exception (IOException → network, SocketTimeoutException → timeout, HttpException → server, else unknown).
- `Stop Details Closed`: on ViewModel `onCleared` or when the app backgrounds while StopDetail is shown, with `durationSeconds` and `refreshes` (successful polls).

### Session Summary and usage profile

- New `SessionTracker` (singleton, Koin) that observes navigation, analytics events (it can be a `Tracker` like `HappyMomentTracker`) and `ProcessLifecycleOwner`. It accumulates counters while in foreground and on ON_STOP sends `Session Summary`, then resets.
- `lastScreen` = destination shown at ON_STOP. A session with `lastScreen` = for_you, `stopViews` = 0 and `arrivalsViews` > 0 is a user who glanced at favorites and left: the main use case we can't see today.
- `sessionType` rules (first match wins): card_checker (only Cards screens, no stop views); waiter (one StopDetail stay > 180 s, or same stopId in 2+ sessions within 20 min, kept in DataStore); explorer (used Map Stop Clicked, Lines or Search); glancer (< 60 s and at least one Arrivals Displayed); other.
- Keep the last 30 days of `(date, sessionType, weekday)` in DataStore. After each summary, compute and `identify()` `usageProfile` (most frequent type; mixed if none > 50%) and `isCommuter` (3+ distinct weekdays in the last 7 days and favoritesCount ≥ 1).

### User properties

New `UserPropertiesTracker` (or extend `AmplitudeTracker`) that observes `SessionService.observeCurrentUser()`, `FavoriteRepository.observeFavorites()`, cards repository, `NfcStateManager`, `NightModeDataSource` and location permission, and calls `amplitude.identify(Identify().set(...))` on change. Mirror to Firebase with `setUserProperty` (names ≤ 24 chars; all fit).

### Other new events

Login (StopDetailScreen `LoginRequired` snackbar = `Login Prompt Shown`; action performed = `Login Started` trigger favorite; SettingsViewModel `onLoginClick` = `Login Started` trigger settings; results from `manualSignIn`), Logout Clicked, Route Direction Switched, Map Explored (once per session, SevMap gesture camera move), Bottom Sheet Changed (settled detent after user drag), Location Permission Result (nearby widget and map), Search Performed (debounce 1 s in SearchViewModel; `query` only when zero results), Edit Favorites Saved / Cancelled (diff in EditFavoritesViewModel), Card Deleted, Cards Reordered, Night Mode Changed, App Opened From NFC (MainActivity NFC intent; cold if it is the launching intent).

### Firebase

`FirebaseTracker` stringifies unknown types: booleans and lists end up as strings. Add `is Boolean -> param(key, if (value) 1L else 0L)` and join lists with commas. Firebase event names must be ≤ 40 chars (the longest, `Nearby_Stops_Location_Permission_Clicked`, is exactly 40).

### Tests

Follow the existing style (mockito-kotlin `mock<Analytics>()` + `verify`). Add a small `FakeAnalytics`/recording tracker if verifying sequences gets verbose. Cover the items in AC #4.

### Refined plan after code research (2026-10-08)

1. Navigation: SevNavigator exposes a `transition` StateFlow of `NavigationTransition(destination, trigger, previous)`; navigate = navigation, navigateBack/popToRoot = back, initial = launch. `StopDetail` gets a serializable `source: StopDetailSource` (default OTHER).
2. Screen views move out of composition into a process-level `ScreenViewTracker` started in `SevApplication.onCreate`, so activity recreation no longer re-tracks (F1, F2). App Started also moves to `SevApplication.onCreate`. Line Stops Viewed skips transitions from a LineStops with the same lineId (F3); Route Direction Switched is tracked in `LineRouteViewModel.onRouteSelected`.
3. `Tracker` gets `setUserProperty(UserProperty)` (default no-op). Amplitude uses `identify`, Firebase `setUserProperty`. Analytics.setUserProperty is not gated by the opt-in because both SDKs already drop data while opted out.
4. `UserPropertiesTracker` and `SessionTracker` are Trackers (like HappyMomentTracker) that receive Analytics lazily to avoid the DI cycle.
5. Stop Details Closed / Arrivals Displayed in StopDetail are driven by a screen-visible lifecycle effect (the sheet keeps the StopDetail composed under full-screen destinations, and the ViewModel is activity scoped, so onCleared is not per view).
6. Favorites/nearby Arrivals Displayed: a small aggregator fed by each list item's loaded state, once per For You view.
7. Session classification and usage profile as pure functions, unit tested; history kept in DataStore.
8. Remaining events, renames (F5), Card Check Completed (F6), Amplitude logout fix (F4), Firebase boolean/list params.
9. Unit tests for AC #4, docs update, screenshot tests.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented on branch feat/analytics-tracking-plan (not pushed yet).

Done:
- All 24 new and 13 changed events from doc-2, the 8 user properties (Amplitude identify, Firebase setUserProperty) and fixes F1-F6.
- Screen views moved to a process-level ScreenViewTracker fed by SevNavigator.transitions (NavigationTransition with trigger and previous destination). App Started moved to SevApplication.onCreate.
- SessionTracker (ProcessLifecycleOwner) + SessionClassifier + SessionHistoryDataSource for Session Summary, usageProfile and isCommuter. Map Explored is deduplicated per session through SessionTracker.markMapExplored().
- Stop Details Closed and stop detail Arrivals Displayed are driven by a LifecycleStartEffect that only runs while the StopDetail is the current destination (the sheet keeps it composed under full-screen destinations and the ViewModel is activity scoped). Favorites/nearby Arrivals Displayed use ArrivalsDisplayTracker, fed by each list item.
- Item arrival loops now rethrow CancellationException, so leaving the screen is not tracked as Arrivals Failed.

Interpretation decisions (also listed in doc-2 'Implementation notes'): arrival_line source has no entry point and is not sent; favorites widget login button uses trigger favorite; Bottom Sheet Changed compares the 3-value state; usageProfile ignores 'other' sessions; card_checker includes the screen shown at session start.

Verification:
- ./gradlew test: 139 tests, 0 failures. New: ScreenViewEventsTest (sources, triggers, Line Stops not re-firing), StopDetailViewModelTest (Arrivals Displayed once per view, latency, Stop Details Closed refreshes, Arrivals Failed per poll), SessionClassifierTest, FavoritesDiffTest, CardViewModelTest.
- ./gradlew validateDebugScreenshotTest: 85 tests, 0 failures.
- ./gradlew :app:compileReleaseKotlin OK. lintDebug: 1 pre-existing error (themes.xml windowSplashScreenBackground NewApi), none in changed files.

Pending: AC #5 manual check in the Amplitude dev project needs a device (none connected). AC #1-#3 left unchecked until that runtime check confirms names and properties as received. DoD #1 (push to master) pending.

Review follow-up:
- Added Session Summary.lastScreen (destination at ON_STOP, snake_case: for_you, lines, line_stops, stop_detail, cards, cards_help, edit_favorites, search, settings). Added to doc-2. Values chosen to match the for_you example in the plan; confirm they match the Amplitude tracking plan.
- Card Top Up Clicked: the branch already sends cardType (Clicks.kt). The event seen at 12:11 with type came from a build without the rename (Clicks.kt was changed at 12:21, and the branch was never installed on a device). Added RenamedPropertiesTest to lock the F5 property names.
- ./gradlew :app:testDebugUnitTest: 144 tests, 0 failures. compileReleaseKotlin OK.

Opened PR #28 from feat/analytics-tracking-plan.
<!-- SECTION:NOTES:END -->
