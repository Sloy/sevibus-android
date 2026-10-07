---
id: SEVAND-7
title: Stop with no arriving buses shows loading skeleton forever
status: Done
assignee:
  - '@claude'
created_date: '2026-10-06 18:27'
updated_date: '2026-10-07 13:29'
labels:
  - e2e
dependencies: []
priority: medium
type: bug
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When the arrivals request returned an empty list, StopDetailViewModel mapped it to ArrivalsState.Loading, the same value it used before the first response, so the stop detail could show the loading skeleton forever.

An empty response from the API is not the cause: RemoteBusRepository returns BusArrival.NotAvailable for every line of the stop without buses, so the screen shows "No disponible" for each line (verified with STOP-04 on master). The repository only returns an empty list when the stop has no routes in the local database, which means corrupted or incomplete transit data.

Expected: that case is shown as an error instead of the skeleton.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 An empty list from the repository stops showing the loading skeleton
- [x] #2 An empty list from the repository shows the arrivals error banner, reworded to: Ocurrió un error al cargar los tiempos. Comprueba tu conexión o inténtalo más tarde
- [x] #3 E2E flow STOP-04 passes: a stop with no buses shows No disponible for every line
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Emit null before the first response so the skeleton is only shown while waiting.
2. Treat an empty list from the repository as an error (EmptyArrivalsException) so the existing failure banner is shown and the case is logged.
3. Reword stopdetail_error_loading_arrivals (es and en).
4. Fix STOP-04 to assert the real behaviour for an empty API response (No disponible per line).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
STOP-04 failed on master only because it waited for a no service text that does not exist: with arrivals-844 set to empty, the screen showed No disponible for lines 25, 26 and A5 and no skeleton. Stop 844 has routes in the mock data, so the empty-list path was never hit; the skeleton in the task description came from reading the code. The previous out-of-service approach in PR #22 was dropped because it read the same routes as the repository and could only ever return an empty list. The failure banner string changed, so the StopDetailScreenFailedArrivalsPreview screenshot references need updating (update-screenshots label on the PR). Validation: testDebugUnitTest passes; Maestro STOP-01 to STOP-06, CONN-01 and CARDS-01 to CARDS-07 pass on an API 30 emulator. The empty-list error path itself is not covered by a Maestro flow, since it needs a stop without routes.

Added StopDetailViewModelTest: loading until the first response, loaded arrivals, and Failed for an empty list. Regenerated the StopDetailScreenFailedArrivalsPreview references locally (light and dark) with the new copy; validateDebugScreenshotTest and testDebugUnitTest pass.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
The stop detail no longer shows the loading skeleton forever for an empty arrivals list. The ViewModel emits null while waiting for the first response, and an empty list from the repository (only possible with a stop without routes, i.e. broken data) shows the existing failure banner, reworded to 'Ocurrió un error al cargar los tiempos. Comprueba tu conexión o inténtalo más tarde'. An empty API response keeps showing No disponible per line, as before. STOP-04 now asserts that. Verified with StopDetailViewModelTest, testDebugUnitTest, validateDebugScreenshotTest and Maestro STOP-01 to STOP-06 and CONN-01 on an API 30 emulator.
<!-- SECTION:FINAL_SUMMARY:END -->
