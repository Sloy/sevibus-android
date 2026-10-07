---
id: SEVAND-2
title: Detect orphan screenshot references
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-06 23:04'
updated_date: '2026-10-07 06:05'
labels: []
dependencies: []
type: enhancement
ordinal: 2000
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
Decision: neither validation nor generation deletes anything, they only fail. Deleting is a source change, so it only happens on explicit actions (update, the delete task or the CI label).
Verified locally: validate 73/73 with 0 orphans. Removing @ScreenshotTest from AlertWidgetPreview makes the check fail listing its image, and the delete task removes it. Replacing @PreviewLightDark with @Preview in StopTimelineElementPreview and updating that test deletes the dark reference and leaves the light one byte-identical. assembleDebug, testDebugUnitTest and check don't run the new tasks. Report script tested with and without orphans.
Blocker: this session's GitHub App lacks the workflows permission, so it can't push .github/workflows/screenshot-tests.yml (rejected by both git push and the API). The CI changes (screenshot_report.py, the workflow and the CI paragraph in CLAUDE.md) are in ci-orphans.patch, to be applied manually. AC #3 is pending on that patch.
<!-- SECTION:NOTES:END -->
