---
id: SEVAND-13
title: Stop map markers from freezing the main thread
status: To Do
assignee: []
created_date: '2026-10-08 08:35'
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
- [ ] #1 On a cold start no single frame adds or updates more stop markers than fits in a frame budget, and Choreographer reports no big skipped frame runs caused by the stop markers
- [ ] #2 Stops inside the visible map area appear in the first frame after the stops load
- [ ] #3 All stops end up on the map after loading finishes, so panning fast never shows markers appearing late
- [ ] #4 Changing the zoom level swaps the stop icons without freezing the map, updating the visible markers first
- [ ] #5 Hiding and showing the stops layer again (hideOnZoom in the lines overview) reloads it progressively instead of in one frame
- [ ] #6 Clicking a stop marker keeps opening the stop, and the debug Map module marker toggles keep working
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [ ] #2 (For changes affecting UI) Screenshot test pass
<!-- DOD:END -->
