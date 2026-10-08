---
id: doc-2
title: Analytics tracking plan
type: specification
created_date: '2026-10-08 01:00'
updated_date: '2026-10-08 01:00'
---
Source of truth for Amplitude (and Firebase Analytics) events in the Android app. Implemented by SEVAND-12. Discussion and dashboards live in the "SeviBus — Amplitude analytics plan" doc (Tracking plan tab). Keep this file, the code in `infrastructure/analytics/events/` and the Amplitude tracking plan (project SeviBus Prod) in sync.

61 events: 24 new, 13 changed, 24 unchanged. All ship in one release. Amplitude's tracking plan (SeviBus Prod) mirrors this list; new events are added there as planned, so anything that arrives off-plan shows as unexpected.

## Conventions

- Event names: Title Case, object then past-tense action (`Stop Details Viewed`, `Login Completed`). Screens end in `Viewed`, taps in `Clicked`.
- Property names: camelCase (`stopId`, `durationSeconds`), matching the existing events. Enum values: lowercase snake_case.
- Ids are numbers (`stopId`, `lineId`, Int); `routeId` is a string. Durations in seconds, latency in ms, money in cents.
- Every screen view event carries `trigger`: `launch` (start destination on app start), `navigation` (forward), `back` (back navigation). Charts about intent filter `trigger` = navigation.
- No free text, emails or names in properties. The only exception is `Search Performed.query`, sent only for zero-result searches.
- Autocapture stays as is: sessions, app lifecycles, deep links, frustration interactions.

## User properties

Set with `identify()` whenever the value changes; never sent as event properties.

| Property | Type | Values | Set when |
| --- | --- | --- | --- |
| `isLoggedIn` | boolean | — | Session state changes |
| `favoritesCount` | number | — | Favorites list changes |
| `cardsCount` | number | — | Cards list changes |
| `locationPermission` | enum | granted, denied, not_asked | App start; permission result |
| `nfcState` | enum | enabled, disabled, not_available | App start (NfcStateManager) |
| `nightMode` | enum | follow_system, light, dark | App start; setting changed |
| `usageProfile` | enum | glancer, waiter, explorer, card_checker, mixed | After each Session Summary, from local 30-day counters (most frequent sessionType; mixed if none above 50%) |
| `isCommuter` | boolean | — | After each Session Summary: active 3+ distinct weekdays in the last 7 days and favoritesCount ≥ 1 |

## Session types

Computed in the app for `Session Summary.sessionType`. Thresholds are first guesses; tune after a few weeks of data.

| sessionType | Rule (first match wins, in this order) |
| --- | --- |
| `card_checker` | Only Cards screens visited; no stop views. |
| `waiter` | A single Stop Details stay over 180 s, or the same stopId viewed in 2+ sessions within 20 min (kept locally). |
| `explorer` | Used map stop taps, Lines or Search. |
| `glancer` | Under 60 s, saw arrivals (favorites or one stop), no map, search or lines. |
| `other` | Anything else (e.g. settings only, no arrivals). |

## Events

### App and session

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| App Started | Changed | Once per process, on cold start (Application.onCreate or first Activity creation). Not on recomposition or configuration change. | — |
| App Opened From NFC | New | MainActivity receives an NFC TECH_DISCOVERED intent (a transport card tapped on the phone). | `launchType` enum (cold, warm) |
| Session Summary | New | App goes to background (ProcessLifecycleOwner ON_STOP), once per foreground period. Built from counters kept in memory during the session. | `sessionType` enum (glancer, waiter, explorer, card_checker, other); `durationSeconds` number (seconds in foreground); `stopViews` number (Stop Details Viewed count); `distinctStops` number (distinct stopIds viewed); `arrivalsViews` number (Arrivals Displayed count); `entrySources` string[] (distinct Stop Details Viewed sources); `featuresUsed` string[] (favorites, nearby, map, lines, search, cards, settings) |

### Screens

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| For You Viewed | Changed | Destination becomes For You. | `trigger` enum (navigation, back, launch) |
| Lines Viewed | Changed | Destination becomes Lines. | `trigger` enum (navigation, back, launch) |
| Line Stops Viewed | Changed | Destination becomes LineStops with a different lineId than the current one. Not on route direction switch or highlighted stop change. | `lineId` number; `routeId` string, optional; `trigger` enum (navigation, back, launch) |
| Stop Details Viewed | Changed | Destination becomes StopDetail. | `stopId` number; `source` enum (favorites, nearby, map, search, line_route, arrival_line, back, other); `highlightedLineId` number, optional; `trigger` enum (navigation, back, launch) |
| Cards Viewed | Changed | Destination becomes Cards. | `trigger` enum (navigation, back, launch) |
| Cards Help Viewed | Changed | Destination becomes CardsHelp. | `trigger` enum (navigation, back, launch) |
| Edit Favorites Viewed | Changed | Destination becomes EditFavorites. | `trigger` enum (navigation, back, launch) |
| Search Viewed | Changed | Destination becomes Search. | `trigger` enum (navigation, back, launch) |
| Settings Viewed | Changed | Destination becomes Settings. | `trigger` enum (navigation, back, launch) |
| Stop Details Closed | New | StopDetail screen leaves composition or app goes to background while it is shown. | `stopId` number; `durationSeconds` number; `refreshes` number (successful arrival polls) |

### Arrivals

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Arrivals Displayed | New | First successful arrivals load per screen view. Not on each 20 s poll. | `screen` enum (stop_detail, favorites, nearby); `stopId` number (stop_detail only), optional; `stopCount` number (favorites and nearby only), optional; `arrivalsCount` number; `latencyMs` number |
| Arrivals Failed | New | An arrivals request fails (every failure, polls included). | `screen` enum (stop_detail, favorites, nearby); `stopId` number, optional; `errorType` enum (network, timeout, server, unknown) |

### Stops and lines

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Favorite Stop Clicked | Existing | Tap on a favorite in For You. | `stopId` number |
| Nearby Stop Clicked | Existing | Tap on a nearby stop in For You. | `stopId` number |
| Map Stop Clicked | New | Tap on a stop marker on the map (MapContainer onStopSelected). | `stopId` number |
| Line Route Stop Clicked | New | Tap on a stop in a line's route (LineRouteScreen onStopClick). | `lineId` number; `stopId` number |
| Arrival Clicked | New | Tap on an arrival in stop detail (onArrivalClick). | `lineId` number; `stopId` number |
| Route Direction Switched | New | Route tab changed in a line. | `lineId` number; `routeId` string |
| Line List Clicked | Changed | Tap on a line in Lines. | `lineLabel` string (renamed from `line`) |
| Line Paths Displayed | Existing | Lines section draws line paths on the map. | `pathCount` number |
| Location Button Clicked | Existing | Map location button tapped. | `state` enum (no-permission, inside-sevilla, outside-sevilla) |
| Map Explored | New | First camera move by gesture in a session (SevMap cameraMoveStartedReason GESTURE). Once per session. | — |
| Bottom Sheet Changed | New | Sheet settles on a different detent after a user drag. | `state` enum (collapsed, partial, expanded) |
| For You Tab Clicked | Existing | Tab switched in For You. | `tab` enum (favorites, nearby) |
| Nearby Stops Location Permission Clicked | Existing | Permission button in the nearby tab. | — |
| Location Permission Result | New | System permission dialog returns. | `result` enum (granted, denied); `context` enum (nearby, map) |

### Search

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Search Performed | New | Search term settles for 1 s (debounced), non-blank. | `queryLength` number; `resultsCount` number; `stopResults` number; `lineResults` number; `query` string (only when resultsCount = 0; trimmed, lowercased, max 40 chars), optional |
| Search Result Stop Clicked | Existing | Tap on a stop result. | `stopId` number |
| Search Result Line Clicked | Existing | Tap on a line result. | `lineId` number |

### Favorites

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Add Favorite Clicked | Existing | Favorite added from stop detail (logged in). | `stopId` number |
| Remove Favorite Clicked | Existing | Favorite removed from stop detail. | `stopId` number |
| Edit Favorites Clicked | Existing | Edit button in the favorites widget. | — |
| Edit Favorite Line Clicked | Existing | Line toggled for a favorite in the edit screen. | `isSelected` boolean |
| Edit Favorites Saved | New | Save tapped in Edit Favorites. Counts come from diffing old vs new list. | `renamed` number; `iconChanged` number; `deleted` number; `linesChanged` number; `reordered` boolean |
| Edit Favorites Cancelled | New | Cancel or back from Edit Favorites without saving. | — |

### Login

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Login Prompt Shown | New | LoginRequired event shown (anonymous user tapped add favorite). | `trigger` enum (favorite) |
| Login Started | New | Google sign-in flow launched. | `trigger` enum (favorite, settings) |
| Login Completed | New | manualSignIn returns success. | `trigger` enum (favorite, settings) |
| Login Failed | New | manualSignIn returns failure (including user cancel). | `trigger` enum (favorite, settings); `reason` enum (cancelled, no_credentials, network, unknown) |
| Logout Clicked | New | Logout in settings. | — |

### Cards

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Card Scanned | Existing | Valid serial entered or read by NFC, before lookup. | `scanMethod` enum (nfc, manual) |
| Card Check Completed | New | Card lookup finishes. | `scanMethod` enum (nfc, manual); `result` enum (added, already_saved, not_found, error) |
| Card Added | Existing | New card saved. | `cardType` string |
| Card Deleted | New | Card deleted successfully. | `cardType` string |
| Cards Reordered | New | Reordering saved successfully. | `cardsCount` number |
| Card Top Up Clicked | Changed | Top-up link opened. | `cardType` string (renamed from `type`); `balance` number (cents), optional |
| Card Alert Displayed | Existing | Low or negative balance alert shown in For You. | `balanceType` enum (low, negative) |
| Card Alert View Clicked | Existing | Alert tapped. | — |
| Card Alert Dismiss Clicked | Existing | Alert dismissed. | — |

### Settings and app

| Event | Status | Fires when | Properties |
| --- | --- | --- | --- |
| Night Mode Changed | New | Night mode setting changed. | `mode` enum (follow_system, light, dark) |
| Feedback Clicked | Existing | Feedback in settings. | — |
| Analytics Disabled Clicked | Existing | Analytics toggle switched off (sent before opting out). | — |
| App Update Available | Existing | In-app update available. | — |
| App Update Download Clicked | Existing | Update download tapped. | — |
| App Update Install Clicked | Existing | Update install tapped. | — |
| Review Dialog Requested | Existing | Happy moment reached; review flow launched. | — |
| Review Dialog Dismissed | Changed | Review flow returns success. | `durationSeconds` number (renamed from `duration`) |
| Review Dialog Failed | Existing | Review flow fails. | `reason` string |
