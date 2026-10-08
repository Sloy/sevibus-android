---
id: SEVAND-11
title: Redesign the low-balance warning and Bonobús balance section
status: Done
assignee:
  - '@rafa'
created_date: '2026-10-07 18:54'
updated_date: '2026-10-08 14:50'
labels: []
dependencies: []
ordinal: 10000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Users get a weak low-balance warning on Para ti and no clear way to top up from the Bonobús detail. The redesign is specified in docs/superpowers/specs/2026-10-07-low-balance-redesign-design.md (visual source: the low-balance handoff package).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Para ti shows a single tappable amber card with thumbnail, balance, title, "Toca para recargar" and a dismiss button
- [ ] #2 Bonobús detail shows a balance section with trips chip (low balance only) and top-up button (red when low, grey otherwise), hidden for cards without balance
- [ ] #3 The operations banner is replaced by a one-line info note and the Recarga row is removed from the Datos card
- [ ] #4 Switching cards animates the balance section, chip and button colours when the swipe commits
- [ ] #5 Unit tests and screenshot tests cover the new states, and Maestro CARDS-05 is updated
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [ ] #2 (For changes affecting UI) Screenshot test pass
<!-- DOD:END -->
