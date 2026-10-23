---
id: SEVAND-16
title: Redesign the Bonobús card screen
status: In Progress
assignee: []
created_date: '2026-10-11 11:04'
updated_date: '2026-10-11 16:12'
labels:
  - frontend
dependencies: []
ordinal: 16000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The Bonobús tab only shows balance, card type and a flat list of validations. Passes (monthly, student, etc.) show no validity, expired cards look like regular ones and online purchases or renewals are invisible. The backend is adding additive card status and transaction fields (kind, validity dates, next period, card expiry, pending, balanceAfter) so the app can show a status block per card family, a details card and a recent-activity list. The app must keep working against the old backend.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Card DTOs map the new optional fields and fall back to today's behaviour when they are missing
- [x] #2 Room caches the card status fields (database 11, auto migration)
- [x] #3 Status block for balance, pass and expired cards, details card with serial copy, and activity list grouped by month with merged group trips
- [x] #4 New analytics events Card More Info Clicked and Card Serial Copied, tracking plan updated
- [x] #5 Screenshot tests cover every card case
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [ ] #2 (For changes affecting UI) Screenshot test pass
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
Track FE of the card redesign plan (FE1-FE12): DTO and domain status with old-backend fallbacks, unified transaction model, Room 10->11 auto migration, date formatter, status/details/activity presentation builders, strings, status blocks, details card with copy, activity list, screen assembly with 8 case stubs and screenshot tests, tracking plan.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Verified: ./gradlew test (207 unit tests, 0 failures), validateDebugScreenshotTest (104 screenshots, 0 failures), assembleDebug: BUILD SUCCESSFUL. ./gradlew lint fails on a pre-existing NewApi error in res/values/themes.xml:8 (windowSplashScreenBackground, file untouched by this work); no lint issues in new code.
Case stubs follow the handoff samples (made-up serials 900000101-900000108).
Pending: manual run against the dev backend once the backend card-status release is on dev; PR must flag the zero-balance fix and the display-name changes (cardType values for 154, 105, 110-114).
<!-- SECTION:NOTES:END -->
