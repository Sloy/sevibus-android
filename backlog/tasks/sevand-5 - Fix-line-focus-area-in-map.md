---
id: SEVAND-5
title: Fix line focus area in map
status: Done
assignee:
  - '@claude'
created_date: '2026-10-06 16:14'
updated_date: '2026-10-07 19:49'
labels:
  - map
dependencies: []
priority: high
type: bug
ordinal: 5000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
There is a bug when selecting a line from the list. The map should center on the selected route, fitting the entire line path. But instead it seems to center below it. This cannot be easily tested with unit tests because it involves the map. But it can be reproduced with maestro test and screenshot, and maybe test with verifying screenshots on fixture data.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Open the app. Click on Lines section. Select a line. The entire line is visible on the top portion of the map.
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [x] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Reproduce on an emulator with the map on and confirm the offset visually.
2. Instrument the camera code to find why the target lands south of the line.
3. Add map debug tools (PR #24) to see the camera, padding and fit bounds.
4. Compute the camera fit ourselves (CameraFit) instead of newLatLngBounds, add a margin to the camera target restriction, hide buses when zoomed out.
5. Compare before/after on several lines and tune with the user.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Use maestro MCP to visually explore the map. The map does not export any layout hierarchy, but can be viewed from the screenshots.

Root causes found (verified on Play_Store_API_30 with markers on, dev backend):
1. minZoomPreference = 13f clamps newLatLngBounds. Line 02 needs zoom ~12.0 to fit above the PartiallyExpanded sheet. When the zoom is clamped, Maps places the bounds center at the view center instead of the padded-area center, so the camera target ends ~234dp (at zoom 13) south of the line: target 37.352 vs line center 37.384. With minZoomPreference = 11f: target 37.3837, zoom 12.01, whole line visible.
2. Initial camera used Stubs.locationInitial (37.3614, -5.9838), an old offset that compensated for a map without sheet padding. With padding it lands south of the city. Replaced with SEVILLA_CENTER (37.3886, -5.9900).
Exploratory pass OK: line 02 both directions, C1, EA (widest), stop from line (StopAndLineSelected), stop from Nearby (StopSelected), launch outside Sevilla.

Round 2 (2026-10-07), after feedback that min zoom 11 zoomed out too far:
- Third cause: latLngBoundsForCameraTarget applies to the center of the whole map view, so with the sheet open and the camera zoomed out it pushed southern lines (03) north.
- Fix on branch fix/line-focus (local commit fac0477, stacked on PR #24 debug map module and PR #23 initial center): CameraFit computes target and zoom to fit stops + path between the status bar and the sheet (24dp margin, zoom 11-16), applied with newLatLngZoom. Camera target bounds get a 0.15 deg margin. Buses hidden below zoom 12. CameraFitTest covers the math.
- Before/after report for lines 01, 02, 03, 10, 22, C1, EA: https://claude.ai/artifact/Gr4a3Hqe5ytaEWgDcNrecz
- Open questions for the user: fit area top below the search bar or the status bar, min zoom 11 vs 12, bus threshold, margin. Stop from line (StopAndLineSelected) on the first stop puts the selected pin near the sheet edge and the next stop under the search bar.

Round 3 (2026-10-07): rebased on master after #23 and #24 merged. Bus threshold 13, fit margin 8dp above and below (24dp sides), zoom tick haptic in the Camera chip. Screenshot tests pass. Maestro map position test dropped at the user's request; CameraFitTest and the before/after report cover it.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Lines now fit in the visible part of the map. Causes: minZoomPreference 13 clamped newLatLngBounds, and Maps then ignored the sheet padding; latLngBoundsForCameraTarget pushed southern lines north because it applies to the whole map center; the fit wasted the space under the bars. CameraFit computes target and zoom to fit stops and path from 8dp below the screen top to 8dp above the sheet (24dp sides), zoom 11-16; camera target bounds get a 0.15 deg margin; buses hidden below zoom 13. Shipped in #25, with the initial center fix in #23 and the Map debug module in #24. Verified with CameraFitTest, validateDebugScreenshotTest, emulator before/after captures of lines 01, 02, 03, 10, 22, C1 and EA (https://claude.ai/artifact/Gr4a3Hqe5ytaEWgDcNrecz), stop from line and from Nearby with all markers on, and the user's manual check on device.
<!-- SECTION:FINAL_SUMMARY:END -->
