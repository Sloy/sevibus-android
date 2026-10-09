---
id: SEVAND-14
title: Stop markers showing updating with delay
status: Done
assignee:
  - '@claude'
created_date: '2026-10-08 14:47'
updated_date: '2026-10-09 08:22'
due_date: '2026-10-09'
labels: []
dependencies: []
priority: high
type: bug
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The optimisation attempt in https://github.com/Sloy/sevibus-android/pull/27 introduced an undesired behavior: Huge updates of markers happen in batches.

How to easily reproduce:
- Open the app with location enabled, zommed into your position (zoom=17). It's showing markers for all stops
- Select lines menu
- Click on a line
- The map zooms out to fit the line path (zoom=~12.75)

Expected:
- The stop markers disappear immediately because of the zoom threashold

Actual:
- The map shows all stops for a split second, colored gray
- The stops start hiding from the center out with a visible ripple effect.

This bug was introduced by a startup performance optimization mainly for debug builds, updating markers in batches to avoid blocking the main thread. 
Either revert the optimization, or apply it only on startup.
<!-- SECTION:DESCRIPTION:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [x] #1 Submitted to master, or to an open PR that will merge to master.
- [x] #2 (For changes affecting UI) Screenshot test pass
- [x] #3 Manually tested on a real device by a human
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Keep progressive (batched) icon updates only for the first load of the generic stops layer.
2. After the first load, apply icon swaps and hides in a single pass, as before PR #27.
3. Unit test ProgressiveStopIcons for both modes.
4. Manual check on device by the user.

5. Gray flash remained: the camera fit waits for the bottom sheet before animating, so the stops turned gray at close zoom first. Marker layers now use the lower of the current zoom and the zoom of the pending camera animation (line fit or stop focus), so stops hide as soon as the line is selected.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
ProgressiveStopIcons batches icon updates only until the first non-empty load with a visible icon completes. Later swaps (zoom, color) and hides apply in a single pass. Unit tests added. Pending manual check on device.

First fix wasn't enough on device: gray stops still flashed. SevMap now anticipates the camera fit zoom for the marker layers.

Checked the build before PR #27 on device: the grey flash already happened there. PR #27 only introduced the ripple (batched hiding), fixed by limiting batching to the first load. The grey flash is a pre-existing issue, addressed by anticipating the camera fit zoom in SevMap.

Validation: unit tests and validateDebugScreenshotTest pass. Manually verified on device by the user: no ripple, no grey flash.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Ripple (regression from PR #27): ProgressiveStopIcons now batches icon updates only until the first load completes; later zoom/color swaps and hides apply in a single pass. Grey flash (pre-existing, confirmed on the build before PR #27): the camera fit waits for the bottom sheet, so marker layers now use the lower of the current zoom and the pending camera animation zoom, hiding stops as soon as a line is selected. Verified with unit tests, screenshot tests and a manual check on a real device.
<!-- SECTION:FINAL_SUMMARY:END -->
