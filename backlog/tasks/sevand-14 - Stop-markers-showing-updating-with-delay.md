---
id: SEVAND-14
title: Stop markers showing updating with delay
status: To Do
assignee: []
created_date: '2026-10-08 14:47'
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
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [ ] #2 (For changes affecting UI) Screenshot test pass
- [ ] #3 Manually tested on a real device by a human
<!-- DOD:END -->
