---
id: SEVAND-1
title: ComposablePreviewScanner
status: Done
assignee:
  - '@claude'
created_date: '2026-10-06 15:38'
updated_date: '2026-10-07 07:12'
labels: []
dependencies: []
priority: low
type: enhancement
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Investigate https://github.com/sergio-sastre/ComposablePreviewScanner

Te intent is to generate screenshots automatically from compose Previews. We're using compose official plugin, which requires creating files
in screenshotTest source set, with functions annotated with @PreviewTest. This require manual updates to the screenshot sources, which can
lead to forgotten screenshots or cases. (eg ScreensScreenshotTests)

I want to avoid this duplication by directly using previews from the main source set, adding a special annotation to them, and having them
generate the screenshots code necessary.

ComposablePreviewScanner sounds like a project that does precisely that. Investigate it to check if it suits what I want. If not, another
alternative is writing a custom kotlin compiler plugin to auto generate the screenshots code.
<!-- SECTION:DESCRIPTION:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Pushed to remote's master branch
- [x] #2 Screenshot tests passed
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Add a @ScreenshotTest annotation in main, with the suite as a parameter (Screens / Components).
2. Add a Gradle task in buildSrc that reads the main .kt files (no compilation) and generates ScreensScreenshotTests.kt and ComponentsScreenshotTests.kt in build/generated/screenshotTest, with @Preview(locale = "es") + @PreviewTest. Cacheable and configuration-cache compatible.
3. Wire it only into the screenshotTest compilation (lazy registration), so assembleDebug, installDebug and test don't run it.
4. Fail the build if an annotated preview is private or can't be parsed.
5. Annotate the 62 previews that have a wrapper today and delete the hand-written wrappers.
6. Measure with --profile / --dry-run that assembleDebug doesn't run the task, and how long it takes in validateDebugScreenshotTest.
7. Validate with validateDebugScreenshotTest and update CLAUDE.md.
8. Support day/night previews: @PreviewLightDark or a night uiMode also generates a dark @Preview. Fix the clock-dependent stub that made LineElementPreview fail, in the same PR.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Investigation of ComposablePreviewScanner (CPS):
- CPS only scans previews at runtime (ClassGraph) and returns ComposablePreview objects. It doesn't render, so it needs Roborazzi, Paparazzi or instrumentation tests.
- It doesn't integrate with Compose Preview Screenshot Testing (the official plugin), which only discovers @PreviewTest functions compiled in the screenshotTest source set. Same with alpha16 and AGP test suites (needs AGP 9.5.0-alpha03, the project uses 9.4.1).
- Conclusion: CPS doesn't fit while keeping the official plugin. Generating the wrappers with a Gradle task in buildSrc does. A Kotlin compiler plugin was discarded: unstable K2 API, it would run on every main compilation, and it can't add code to the screenshotTest compilation.

Implementation:
- @ScreenshotTest(ScreenshotSuite.X) annotation in main, GenerateScreenshotTestsTask in buildSrc, wired with androidComponents.onVariants + addGeneratedSourceDirectory.
- 62 previews annotated, hand-written wrappers deleted, references renamed after the preview (same hash b2db1d68).
- Day/night: 11 previews generate a dark variant (*_f6f1fda3_0.png). The light variant keeps its hash.
- Stubs: fixed LocalTime.MIN–MAX schedule instead of LocalTime.now(). Updated the LineElementPreview, LinesScreenPreview and SearchScreenResultsPreview references, which depended on the time of day.

Verification:
- Local validateDebugScreenshotTest: 73/73. testDebugUnitTest passing.
- Performance: assembleDebug, installDebug, testDebugUnitTest, lint and check don't run the generator (--dry-run). 33 ms with --rerun, UP-TO-DATE with no .kt changes.
- Controlled failure checked with a private preview.
- CI on PR Sloy/sevibus-android#18: Run Tests, Build APK and Screenshot test results green.

Review follow-up: the setup moved from app/build.gradle.kts to the sevibus.screenshot-tests convention plugin in the build-logic included build (ScreenshotTestsPlugin, GenerateScreenshotTestsTask, OrphanScreenshotReferencesTask). It compiles against the AGP API as compileOnly, because putting AGP on the buildSrc classpath clashes with the versioned com.android.application declaration in the root build. The app only applies the plugin and sets packageName. Generated tests are byte-identical, and validate passes 73/73.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Screenshot tests are generated from previews annotated with @ScreenshotTest(ScreenshotSuite.X), using a buildSrc Gradle task that reads the main .kt files and only runs when screenshotTest compiles (33 ms; assembleDebug, test, lint and check don't run it). The official plugin is kept and day/night previews are supported. A clock-dependent stub was fixed. Verified with 73/73 screenshots locally and in the CI of PR Sloy/sevibus-android#18. DoD #1 (on master) completes when the PR is merged.
<!-- SECTION:FINAL_SUMMARY:END -->
