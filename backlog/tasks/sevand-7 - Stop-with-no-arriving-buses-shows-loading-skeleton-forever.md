---
id: SEVAND-7
title: Stop with no arriving buses shows loading skeleton forever
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-06 18:27'
updated_date: '2026-10-07 11:48'
labels:
  - e2e
dependencies: []
priority: medium
type: bug
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When the server returns an empty arrivals list for a stop, `StopDetailViewModel` maps it to `ArrivalsState.Loading` (the `arrivals.isEmpty()` branch), so the stop detail shows the placeholder skeleton rows indefinitely and never says there is no service.

Expected: a clear empty state (e.g. no buses expected at this stop) once the response has arrived.

Steps to reproduce:
1. Open the app and search for "estacion cercanias" (or any stop that currently has no buses, e.g. at night).
2. Open the stop "Parada 844".
3. Make the arrivals endpoint return an empty list (`GET /api/arrivals/844` -> `[]`). In the e2e setup: `output.mocks.set("arrivals-844", "empty")`.
4. Observe the line rows keep showing the loading skeleton and no message appears.

Found by e2e flow STOP-04 (`e2e/flows/stop-detail/stop-04-no-service.yaml`), which is expected to fail until this is fixed. The flow waits for a "no service" message that does not exist yet, so its expected copy needs to be updated to the final Spanish text.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 An empty arrivals response stops showing the loading skeleton
- [ ] #2 The stop detail shows a no-service message for each line or for the stop
- [ ] #3 E2E flow STOP-04 passes with the final Spanish copy
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Tell the initial wait apart from an empty arrivals response in StopDetailViewModel (null = loading).
2. Treat an empty response as out of service: show every line of the stop as BusArrival.NotAvailable (existing state).
3. Inject RouteRepository to get the route of each line.
4. Validate with unit tests, screenshot tests and e2e flow STOP-04.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented in https://github.com/Sloy/sevibus-android/pull/22. The placeholder emission is now null (skeleton) and an empty response maps to ArrivalsState.Loaded with NotAvailable arrivals, the existing out of service state, as requested. Note: RemoteBusRepository already returns NotAvailable for lines missing from the response, so the skeleton seen in STOP-04 may come from a stop without routes in the dev server. CI on the PR: Run Tests, Build APK and Validate Screenshots green. Still to verify by the reviewer: e2e flow STOP-04, whose expected copy must match the NotAvailable text (the e2e folder is not on master, so it was not run). No screenshot preview was added for the empty case.
<!-- SECTION:NOTES:END -->
