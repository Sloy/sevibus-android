---
id: SEVAND-8
title: Speed up the Maestro e2e suite
status: Done
assignee:
  - '@claude'
created_date: '2026-10-07 00:07'
updated_date: '2026-10-07 14:59'
labels:
  - e2e
dependencies: []
priority: medium
ordinal: 8000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The full Maestro suite took about 15 minutes on an emulator and up to 55 minutes when the host or emulator was under load, too long for unattended runs. Find the bottlenecks and remove them without losing coverage.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Debug builds never show the in-app review dialog, so flows don't wait for it
- [x] #2 The HTTP overlay is off by default
- [x] #3 run.sh disables animations and compiles the app ahead of time, and restores the device afterwards
- [x] #4 Flows launch the app with the home map disabled by default, with MAP_MODE=full to bring it back
- [x] #5 The README documents the recommended emulator and what run.sh does to the device
- [x] #6 The full suite runs on the recommended emulator
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Full suite on E2E_API_30 (Google APIs, API 30): 733s vs about 925s before. First pass lost 9 flows when the emulator dropped off adb under host memory pressure, and 5 relaunches (SYNC-01, SYNC-02, CONN-01, CARDS-05 x2) missed debugMapMode; fixed in 3731ed7. With the map on, this image crashes the app on location (old GMS 20.18: LocationServices.API not available). Rerun of the 14 failed flows: 11 pass; CARDS-07 and STOP-04 known (SEVAND-6, SEVAND-7); STOP-05 intermittent app error 'Collection contains no element matching the predicate' in RemoteBusRepository after arrivals recover. Unrelated crash seen once: LineElement.kt:26 routes.first() on a line without routes from the dev server.
<!-- SECTION:NOTES:END -->
