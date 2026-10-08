---
id: SEVAND-9
title: Detect orphan screenshot references
status: To Do
assignee: []
created_date: '2026-10-07 07:49'
updated_date: '2026-10-07 07:49'
labels: []
dependencies: []
type: enhancement
ordinal: 15000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
When a @ScreenshotTest preview is deleted, renamed, or loses its dark variant, its reference images stay in app/src/screenshotTestDebug/reference without any test using them. Detect them and remove them in a controlled way.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 validateDebugScreenshotTest fails and lists the orphan references
- [x] #2 updateDebugScreenshotTest and a dedicated task delete the orphan references
- [ ] #3 The CI comment shows the orphans and the update-screenshots label deletes them
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [ ] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. The generator writes the list of expected references (<package path>/<class>/<test>_<hash>), using the hashes of its two @Preview configurations.
2. OrphanScreenshotReferencesTask compares that list with src/screenshotTest<Variant>/reference: it fails listing the orphans, or deletes them.
3. check<Variant>ScreenshotReferences finalizes validate (fails) and delete<Variant>OrphanScreenshotReferences finalizes update (deletes).
4. CI: the PR comment lists the orphans, and the update-screenshots label (and the master update PR) deletes them.
5. CLAUDE.md.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Done in the SEVAND-1 PR (Sloy/sevibus-android#18):
- The generator lists the expected references, using the hashes of its two @Preview configurations.
- OrphanScreenshotReferencesTask: validateDebugScreenshotTest fails listing the orphans (checkDebugScreenshotReferences). updateDebugScreenshotTest and deleteDebugOrphanScreenshotReferences delete them.
- Verified locally: deleting a preview's annotation fails the check, and the delete task removes its image. Dropping a dark variant and updating deletes the dark image and leaves the light one byte-identical. Builds outside the screenshot tasks don't run it.

Remaining (AC #3): the CI side. The diff is in backlog doc-1 (CI changes for orphan screenshot references). It updates .github/scripts/screenshot_report.py, .github/workflows/screenshot-tests.yml and the CI section of CLAUDE.md. It must be pushed from a local clone, because GitHub rejects workflow changes from a GitHub App without the workflows permission. Until then, CI still fails when there's an orphan, but the PR comment doesn't list it and the update-screenshots label doesn't delete it.
<!-- SECTION:NOTES:END -->
