---
id: SEVAND-2
title: Sync deleted favorites
status: To Do
assignee: []
created_date: '2026-10-06 16:01'
labels:
  - favorites
  - cards
  - backend
dependencies: []
priority: medium
type: bug
ordinal: 2000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Currently, favorites are synced by merging BE and FE data. If I use 2 phones with the same account, and delete one favorite from phone 1, it will remove it from the server database. But when phone 2 syncs, it will detect a missing favorite in the BE and push it, restoring the deleted favorite. This happens because the BE doesn't keep track of deleted items, only current ones. We need to modify the BE to keep a createAt and deletedAt date fields, so that the FE does't override deleted favorites. And adapt the FE sync process to work with this new flow. Same should apply to cards.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 maestro flow simulates the sync process with a deleted favorite
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->
