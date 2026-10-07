---
id: SEVAND-6
title: Card serial with leading zeros is looked up without them
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-06 18:27'
updated_date: '2026-10-07 11:48'
labels:
  - e2e
dependencies: []
priority: medium
type: bug
ordinal: 6000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The Travel Card screen converts the typed serial to a number (`serialNumber.toLong()` in `CardViewModel.onNewCardNumber`), so leading zeros are lost and the app asks the backend for a different card than the one typed.

Behaviour: typing `000000000000` requests `GET /api/card/0?via=manual` and the card is never found. Expected: the serial is sent as typed, `GET /api/card/000000000000`.

Steps to reproduce:
1. Launch the app with a clean state and open the Bonobús (Travel Card) tab.
2. Tap the "Número de serie" field.
3. Type `000000000000` (12 digits).
4. Observe the request in the debug network overlay or the server log: `/api/card/0` is requested.
5. The form stays on "Añadir tarjeta" and the card never loads.

Found by e2e flow CARDS-07 (`e2e/flows/cards/cards-07-leading-zeros.yaml`), which is expected to fail until this is fixed.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Typing a serial with leading zeros requests the card with the serial exactly as typed (12 digits, zeros included)
- [ ] #2 A saved card with leading zeros keeps its full serial when reopened and in its transactions request
- [ ] #3 E2E flow CARDS-07 passes
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Keep CardId as Long in Room and the domain (no migration).
2. Send the serial padded to 12 digits in every card API call (CardId.toApiPath()).
3. Unit test the padding and adapt FakeSevibusApi.
4. Validate with unit tests, screenshot tests and e2e flow CARDS-07.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Implemented in https://github.com/Sloy/sevibus-android/pull/22. API paths for card info, transactions, addUpdateCard and deleteCard now use the 12 digit padded serial, so 000000000000 requests /api/card/000000000000 instead of /api/card/0. Assumes every serial has 12 digits in the API, which is what the form validates. CI on the PR: Run Tests, Build APK and Validate Screenshots green. Still to verify by the reviewer: e2e flow CARDS-07 (the e2e folder is not on master, so it was not run).
<!-- SECTION:NOTES:END -->
