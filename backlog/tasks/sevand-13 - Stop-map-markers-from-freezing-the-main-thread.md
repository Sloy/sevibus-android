---
id: SEVAND-13
title: Stop map markers from freezing the main thread
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-08 08:35'
updated_date: '2026-10-08 09:21'
labels:
  - map
  - performance
dependencies: []
priority: high
ordinal: 11000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
At startup the main thread freezes for about 2.5s (92 and 57 skipped frames on a Pixel, debug build) right after the map loads. Main thread sampling shows two causes in `GenericStopsMakerLayer` (feature/map/layers/MarkerLayers.kt): composing one Compose `Marker` per stop of the city, each calling `GoogleMap.addMarker`, all in the same frame; and then `Marker.setIcon` on every marker in one frame when the zoom level changes the stop icon. The icon swap also freezes the map every time the zoom level changes during normal use, so it is the bigger problem. The freeze also hides UI animations that run at startup, like the For You card alert entrance.

Constraints from the product owner: no clustering. Only rendering the stops inside the visible area was tried before and rejected, because when panning fast the markers appear out of nowhere after the camera moves. Every stop must stay on the map once loaded so panning never shows missing markers. The accepted direction is progressive loading: the stops in the visible area first, then the rest in small batches over the next frames. Icon swaps on zoom level changes must be spread the same way, visible markers first.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 On a cold start no single frame adds or updates more stop markers than fits in a frame budget, and Choreographer reports no big skipped frame runs caused by the stop markers
- [ ] #2 Stops inside the visible map area appear in the first frame after the stops load
- [x] #3 All stops end up on the map after loading finishes, so panning fast never shows markers appearing late
- [x] #4 Changing the zoom level swaps the stop icons without freezing the map, updating the visible markers first
- [x] #5 Hiding and showing the stops layer again (hideOnZoom in the lines overview) reloads it progressively instead of in one frame
- [x] #6 Clicking a stop marker keeps opening the stop, and the debug Map module marker toggles keep working
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [x] #2 (For changes affecting UI) Screenshot test pass
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Keep one icon state per stop (ProgressiveStopIcons) and render each stop as its own keyed composable that emits a Marker only once its icon state is set, so a batch only recomposes the markers it touches.
2. A LaunchedEffect keyed on the stops and the target icon orders the stops by camera priority (inside the visible area first, then by distance to the camera target) and writes the target icon to a fixed number of stops per frame, yielding with withFrameNanos between batches. The same loop covers the first load, icon swaps on zoom or color changes, and hideOnZoom (target icon null removes, showing again reloads).
3. Hoist GenericStopsMakerLayer out of the per-state branches in MarkerLayersByState so changing map state (selecting a stop or a line) keeps the existing markers and only swaps colors progressively instead of recreating every marker.
4. Unit test the camera priority ordering.
5. Tune the batch size on device with Choreographer skipped-frame logs, then check clicks and debug toggles.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified on a Pixel (debug build, 1134 stops), measured with Choreographer logs and dumpsys gfxinfo:
- Cold start: the only skipped-frame run left is the launch frame (~75 frames), also present with the stops hidden. The 92 and 57 frame freezes are gone. All stops load in ~2s, 40 markers per frame.
- Zoom changes Far to Medium to Close: each icon swap takes ~0.5s, 99th percentile frame 28ms, no skipped-frame logs.
- Line selected: colors swap to grey progressively, then the layer hides progressively at Far zoom. Closing the line reloads all 1134 stops in ~0.7s with no long freeze.
- Switching tabs to Lines overview keeps the markers: the layer is now hoisted out of the per-state branches, so changing map state no longer recreates every marker.
- Stop click opens the stop. debugHideStops launch argument (same data source as the debug toggle) hides the stops.
- Unit tests and validateDebugScreenshotTest pass.
AC #2 caveat: visible stops go first, so at Close or Medium zoom they appear in the first batch, the frame after the stops load. At Far zoom the whole city is visible, so the visible stops take the full ~29 batches.
<!-- SECTION:NOTES:END -->
