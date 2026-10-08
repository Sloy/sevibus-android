---
id: SEVAND-12
title: Implement analytics tracking plan
status: To Do
assignee: []
created_date: '2026-10-08 01:00'
updated_date: '2026-10-08 01:00'
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
- [ ] #4 Unit tests cover: Stop Details Viewed `source` for each entry point, `trigger` for launch/navigation/back, Line Stops Viewed not re-firing on route switch, Arrivals Displayed firing once per screen view (not per poll), Session Summary `sessionType` classification rules, Edit Favorites Saved diff counts, Card Check Completed results
- [ ] #5 Manual check on a debug build: each new event appears in the Amplitude dev project (SeviBus Dev) with the right properties; result written in Implementation Notes
- [ ] #6 doc-2 and CLAUDE.md updated if anything changed during implementation
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
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
<!-- SECTION:PLAN:END -->
