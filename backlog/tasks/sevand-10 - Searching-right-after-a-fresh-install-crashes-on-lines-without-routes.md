---
id: SEVAND-10
title: Searching right after a fresh install crashes on lines without routes
status: To Do
assignee: []
created_date: '2026-10-07 11:00'
labels:
  - e2e
dependencies: []
priority: high
type: bug
ordinal: 9000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The app can crash when the user searches before the first transit data sync has finished. `RemoteAndLocalLineRepository.searchLines()` reads the matching lines from the database and their routes through `routeRepository.obtainRoutesByLines()`, which only reads the routes table and, unlike `obtainRoutes()`, does not wait for the routes download. Lines and routes are downloaded separately, so when `/api/lines` answers first the search returns lines with an empty `routes` list. `LineElement` then calls `line.routes.first()` (`LineElement.kt:26`) and the app crashes with `NoSuchElementException: List is empty`.

It happens against the dev backend, where `/api/routes` can answer several seconds after `/api/lines` on a fresh install. Every line on dev has routes, so it is not a data problem. Real users on a slow connection can hit it on their first search after installing.

Steps to reproduce:
1. Clear the app data and launch it against dev.
2. Right away, search for "estacion cercanias" before the routes download finishes.
3. The search results compose lines with no routes and the app crashes.

Seen in the Maestro e2e search flow (SEARCH-02 at the time, run against dev) on two runs: the crash stack points to `LineElementKt.LineElement$lambda$3(LineElement.kt:26)`.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Searching before the routes download finishes never crashes
- [ ] #2 Lines shown by search have their routes, or wait until routes are available
- [ ] #3 LineElement does not crash on a line without routes
- [ ] #4 A unit test covers searchLines() when lines are stored but routes are not yet
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->
