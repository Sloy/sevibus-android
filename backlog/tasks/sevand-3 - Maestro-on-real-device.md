---
id: SEVAND-3
title: Maestro on real device
status: Done
assignee:
  - '@claude'
created_date: '2026-10-06 16:07'
updated_date: '2026-10-06 19:05'
labels:
  - e2e
dependencies: []
priority: low
type: enhancement
ordinal: 3000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Currently maestro tests (e2e folder) can only run on an emulator because they depend on english language, and my real device is in spanish. Since the app is supposed to be in spanish, i'd rather have maestro tests run on spanish always. This means making sure any emulator running is in spanish too. This task needs to migrate the copies to spanish, and add some instruction to change the emulator language before launching the test.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Maestro tests pass in real device in spanish
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Translate flows/subflows to Spanish copies from strings.xml (verify non-resource strings on device).
2. Check mock fixtures for locale dependence.
3. run.sh: require es-* locale, optional emulator language switch, extend screen_off_timeout during run (restore on exit), fail if device locked.
4. README: Spanish prerequisites, emulator language, real device notes.
5. Run suite on real device, fix failures; validate screenshot tests.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Flows and subflows translated to Spanish; run.sh requires es-*, wakes/checks lock, extends screen timeout. CONN-01 rewritten to avoid airplane mode (wireless adb). Card mocks now match urlPath (app sends ?via=manual). Line route flows updated to the direction tabs (Origen/Destino labels are gone). Verified on es-ES emulator (4GB RAM). Known failing: CARDS-07 (SEVAND-6), STOP-04 (SEVAND-7). Not yet re-verified on the real device after the later fixes.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Maestro flows now run in Spanish (the app's default language): flows/subflows use the strings.xml copies, run.sh requires an es-* locale, wakes the device, fails fast if locked and extends the screen timeout for the run, and CONN-01 no longer uses airplane mode so it works over wireless adb. Card mocks match urlPath, line route flows use the direction tabs. Verified on an es-ES emulator: all flows pass except CARDS-07 (SEVAND-6) and STOP-04 (SEVAND-7), which are app bugs. Not re-run on the real device after the final fixes (the full suite takes 20+ minutes); task closed at the owner's request, so acceptance criterion #1 is left unchecked. Screenshot tests not run, nothing in app/ changed.
<!-- SECTION:FINAL_SUMMARY:END -->
