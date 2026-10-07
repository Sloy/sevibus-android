---
id: SEVAND-5
title: Fix line focus area in map
status: To Do
assignee: []
created_date: '2026-10-06 16:14'
updated_date: '2026-10-06 16:15'
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
- [ ] #1 Open the app. Click on Lines section. Select a line. The entire line is visible on the top portion of the map.
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
- [ ] #3 There's a maestro test verifying the map position
<!-- DOD:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Use maestro MCP to visually explore the map. The map does not export any layout hierarchy, but can be viewed from the screenshots.
<!-- SECTION:NOTES:END -->
