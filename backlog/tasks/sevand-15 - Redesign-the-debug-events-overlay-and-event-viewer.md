---
id: SEVAND-15
title: Redesign the debug events overlay and event viewer
status: In Progress
assignee: []
created_date: '2026-10-09 13:17'
updated_date: '2026-10-09 14:30'
labels: []
dependencies: []
priority: medium
type: feature
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
The debug events overlay shows one small pill per event through the shared OverlayLogger and hides it after 9 s. Bursts of events tracked in the same frame are hard to follow, and the Events screen is a flat list without timing, grouping or export, which makes checking analytics against the tracking plan slow. A design handoff (kept outside the repo) defines two live overlays (a spring stack, the default, and a timeline rail behind a checkbox) and an Events screen with Timeline and Journey views. Brainstorming decisions: push crowding only (no leaders mode); HTTP overlay unchanged and may overlap; static bottom anchor passed from App.kt; whole store copied as JSON; agreed search semantics; lanes tap scrolls to the row; debug-menu previews use stock DebugPreviewTheme and get screenshot tests through the build-logic generator, validated in CI; Analytics delivers events in call order.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 CapturedEvent has timestampMillis set by OverlayTracker from an injectable clock, unit tested
- [x] #2 Analytics delivers tracking calls to every tracker in call order, unit tested
- [x] #3 The events overlay no longer uses OverlayLogger; HTTP overlay items keep working and their tests pass
- [ ] #4 EventsDebugModuleState.useTimelineRail is persisted and defaults to false; the "Use timeline rail" checkbox is only shown while "Show events on overlay" is on; View all (n) and Clear keep working; disabling the overlay removes every chip at once
- [x] #5 Spring stack: 5 s lifetime (full alpha 2 s, linear to 0.5), draining timer line, coalescing ×N within 3 s of the newest chip with lifetime restart and bump, 3/9 dp burst/normal gaps, 45 ms same-frame stagger, fold pile with "+N older", start ellipsis at 330 dp; layout unit tested
- [x] #6 Timeline rail: strip, centre line, 5 s ticks, pulsing now head, phone/diamond/circle markers with white ring, 31 dp/s drift, top fade, removal after window + 1.5 s, 9 s labels, push spacing 26/7 dp, burst links, screen bands; layout unit tested
- [ ] #7 Both overlays ignore touches and accessibility, and are anchored by a bottom padding passed from App.kt
- [x] #8 Events screen: top bar Events (n) with back and Clear, Timeline/Journey segmented control, search field, pinned Copy session as JSON, empty state "No events yet"
- [ ] #9 Event rows: time, delta, type marker, up to 3 inline properties + "+N more", expandable property box with full timestamp, rows without properties not expandable; expansion survives configuration changes
- [ ] #10 Gap markers for gaps of 10 s or more labelled quiet or in background; Session Summary card collapsed/expanded with uppercase sessionType chip and formatted duration
- [ ] #11 Journey: bands named after the view, newest first with the view as last row, duration chip or now, (resumed) band from lastScreen, loose rows before the first view; lanes card follows the scroll and tapping a mark scrolls to its row
- [ ] #12 Search filters both views with the agreed semantics; Copy session as JSON copies the whole store oldest first with timestampMillis and confirms with a snackbar
- [x] #13 Viewer logic unit tested: formatting, gaps, timeline and journey items, search, lanes, JSON export
- [x] #14 Every visual component and screen has a @PreviewLightDark preview with the handoff data, generated as debug-menu screenshot tests that CI validates; each one is compared with its handoff reference and the differences are recorded
- [x] #15 Release build unaffected: debug-menu-noop mirrors the new DebugMenuHost parameters
- [ ] #16 ./gradlew test lint and validateDebugScreenshotTest pass
<!-- AC:END -->

## Definition of Done
<!-- DOD:BEGIN -->
- [ ] #1 Submitted to master, or to an open PR that will merge to master.
- [x] #2 (For changes affecting UI) Screenshot test pass
<!-- DOD:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
# Debug events overlay and viewer redesign: implementation plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the debug events overlay chip and the Events screen with the redesigned spring stack (default) and timeline rail overlays, and a Timeline/Journey event viewer.

**Architecture:** Every layout rule is a pure Kotlin function of `(events, nowMillis)` or `(events, query)` in `:debug-menu`, unit tested first. Compose UIs only animate towards the targets those functions return. The events overlay gets its own slot in `DebugMenuHost` and is fed by `EventStore`. `OverlayLogger` stays for HTTP only.

**Tech Stack:** Kotlin, Jetpack Compose (BOM 2025.09.00, Material3), kotlinx.serialization, JUnit 4 + Strikt + mockito-kotlin + kotlinx-coroutines-test, Compose Preview Screenshot Testing through the `sevibus.screenshot-tests` plugin in `build-logic`.

**Spec:** the handoff lives outside the repo, in the main checkout's untracked `sevibus-debug-events-handoff/` folder: `README.md`, `docs/01`–`docs/09`, `screenshots/`, `components/`, `animations/`, `design-source/*.dc.html`. Below it's called `$HANDOFF`, the absolute path being `/Users/rafa/ShadowMosses/sevibus/sevibus-android/sevibus-debug-events-handoff`. The decisions taken in brainstorming override it and are listed in Global Constraints.

## Global Constraints

- English only in code, comments, commits and docs. Public repo: no session links, no personal data, no secrets. No `Co-Authored-By` or session links in commits.
- Don't write inline comments: names must explain the code (repo convention). KDoc on public declarations is fine, following the existing style.
- `:debug-menu` stays self-contained: no `com.sloy.sevibus` imports, no app resources, no Koin. Previews use `DebugPreviewTheme` and must not draw any SeviBus UI (no map, no SevTheme). Differences with the handoff PNGs caused by that are intended.
- `:debug-menu-noop` mirrors only the API used from `app/src/main`.
- Release builds unaffected.
- Event types stay as in `EventType.of`: name ends with `Clicked` = click, `Viewed` = view, anything else = other.
- Overlay A (stack) parameters: lifetime 5 000 ms, hold 2 000 ms, fade to 0.5, exit 450 ms, coalesce window 3 000 ms, burst window 150 ms, stagger 45 ms, chip height 28 dp, gaps 3 dp (burst) / 9 dp, top reserve 160 dp, end inset 12 dp, max chip width 330 dp.
- Overlay B (rail) parameters: 31 dp/s, rail height 540 dp, labels 9 s (hold 2 s, fade to 0.55), label gap 26 dp, dot gap 7 dp, burst link < 150 ms, edge fade 90 dp, removal after window + 1.5 s, pop 40 ms + 60 ms stagger, ticks every 5 s. **Push mode only** (leaders mode is out of scope).
- Overlay bottom anchor: a static `overlayBottomPadding` passed from `App.kt` to `DebugMenuHost`, measured inside the `safeDrawing` insets. The stack's bottom sits on it. The rail strip's bottom sits 10 dp below it and the marker origin 4 dp below it (114/120 vs 124 in the design).
- The HTTP overlay keeps its look and position (bottom-end) and may overlap the events overlay (accepted). The floating debug button may cover the overlay (accepted).
- The overlays never take touches and are invisible to accessibility (`pointerInteropFilter { false }` + `clearAndSetSemantics {}`).
- "Use timeline rail" is a checkbox `DebugCell`. It's **hidden** (not disabled) while "Show events on overlay" is off. It's persisted in `EventsDebugModuleState.useTimelineRail = false`.
- Ordering: `Analytics` dispatches tracking calls through one FIFO queue, so every tracker receives events in call order.
- Copy session as JSON: the whole `EventStore`, oldest first, as a JSON array of `{name, timestamp, timestampMillis, properties}`, copied to the clipboard and confirmed with a snackbar `Copied N events`.
- Search: case-insensitive. A query matches the name, any key, any value, `key=value` or `key value`. It applies to both views. Journey hides bands without matching rows. The Session Summary card shows when it matches or the query is empty.
- Journey lanes card: follows the scroll. Tapping a mark scrolls the list to its row (nearest mark within 24 dp, or the screen bar under the finger).
- Screenshot tests: debug-menu previews get `@ScreenshotTest(ScreenshotSuite.X)` from a debug-menu copy of the annotation, are generated by the existing `sevibus.screenshot-tests` plugin (extended to library modules) and are validated in CI like the app's.
- Animation easings (docs/08): `SpringyOut = CubicBezierEasing(0.30f, 1.35f, 0.45f, 1f)`, `PopOut = (0.30f, 1.70f, 0.50f, 1f)`, `LabelOut = (0.30f, 1.50f, 0.50f, 1f)`, `ExitAccel = (0.30f, 0f, 0.80f, 0.15f)`, `CssEaseIn = (0.42f, 0f, 1f, 1f)`, `CssEaseOut = (0f, 0f, 0.58f, 1f)`, `CssEase = (0.25f, 0.1f, 0.25f, 1f)`.

## Review Focus

1. **Filter with no results:** the store has events but none match the query. Expect a centred `No matching events` instead of a blank list, and the lanes card hidden. The test lives in Task 7 (`timelineItems` returns empty) and the UI branch in Task 12.
2. **Clear while the overlay is showing chips:** `EventStore.clear()` empties the list, and the overlay must drop every chip at once without crashing on the missing keys. Covered by the Task 4 test `empty events give an empty frame`. The UI keys chips with `key(chip.key)`.
3. **Store at capacity (200) with a long-lived Journey:** events fall out of the store, so the oldest band may lose its `… Viewed` row. Expect those orphan events to render as loose rows, not to crash. Covered by the Task 8 test `events before any view are loose rows`.
4. **Session Summary without `durationSeconds` or `lastScreen`** (null properties are dropped): the card shows no duration and the resumed band is named `Resumed`. Covered by Task 8 (`resumed band without lastScreen`) and Task 11 (`SessionSummaryCard` handles a missing duration).
5. **Clock jumps / out-of-order timestamps** (e.g. an event with an older timestamp than the previous one): the layouts sort chronologically and never produce negative ages. Covered by the Task 4 test `future events are not shown yet` and the Task 7 test `sorts by timestamp`.

---

## File map

`:debug-menu` (`debug-menu/src/main/java/com/sloy/debugmenu/`)

| File | Responsibility |
| --- | --- |
| `base/ScreenshotTest.kt` (new) | `@ScreenshotTest` + `ScreenshotSuite` for debug-menu previews |
| `events/CapturedEvent.kt` (modify) | Model with `timestampMillis`, `type`, `chronological()` |
| `events/EventFormat.kt` (new) | Clock, duration, delta formatting |
| `events/EventsJson.kt` (new) | JSON export (replaces `toPrettyJson`) |
| `events/EventType.kt` (modify) | Accent tokens, `EventColors`; `EventTypeIcon` removed |
| `events/EventText.kt` (new) | Monospace text styles |
| `events/overlay/StackLayout.kt` (new) | Pure stack frame |
| `events/overlay/RailLayout.kt` (new) | Pure rail frame |
| `events/overlay/OverlayStyle.kt` (new) | Overlay colours and easings |
| `events/overlay/OverlayDemo.kt` (new) | Demo sequence of docs/03 for previews |
| `events/overlay/EventsStackOverlay.kt` (new) | Stack UI + previews |
| `events/overlay/EventsRailOverlay.kt` (new) | Rail UI + previews |
| `events/overlay/EventsOverlay.kt` (new) | Public entry: state, clock, mode switch |
| `events/viewer/EventSearch.kt` (new) | Query matching |
| `events/viewer/ViewerItems.kt` (new) | Timeline and Journey item lists |
| `events/viewer/Lanes.kt` (new) | Lanes model + hit testing |
| `events/viewer/ViewerSampleData.kt` (new) | `DATA` of `ViewerTimeline.dc.html` |
| `events/viewer/EventRow.kt`, `PropertyBox.kt`, `GapMarker.kt`, `SessionSummaryCard.kt`, `ScreenBand.kt`, `LanesCard.kt`, `ViewerChrome.kt` (new) | Viewer components + previews |
| `events/EventLogScreen.kt` (rewrite) | Events screen (Timeline/Journey) |
| `events/EventsModule.kt`, `EventsDebugModuleState.kt`, `EventsDebugModuleViewModel.kt` (modify) | Rail checkbox, no `OverlayLogger` |
| `events/EventOverlayLoggerItem.kt` (delete) | |
| `overlay/DebugMenuHost.kt` (modify) | `overlay` slot + `overlayBottomPadding` |

Elsewhere: `build-logic/src/main/kotlin/ScreenshotTestsPlugin.kt`, `debug-menu/build.gradle.kts`, `debug-menu/src/screenshotTest/AndroidManifest.xml`, `.github/workflows/screenshot-tests.yml`, `.github/scripts/screenshot_report.py`, `.github/scripts/updated_references.sh`, `debug-menu-noop/.../overlay/DebugMenuHost.kt`, `app/src/main/java/com/sloy/sevibus/App.kt`, `app/src/main/.../infrastructure/analytics/Analytics.kt`, `app/src/debug/.../feature/debug/events/OverlayTracker.kt`, `app/src/debug/.../feature/debug/SevDebugOverlay.kt` (new), `app/src/release/.../feature/debug/SevDebugOverlay.kt` (new), `app/src/debug/.../feature/debug/SevDebugMenu.kt`, `app/src/debug/.../infrastructure/BuildVariantDI.kt`, `CLAUDE.md`.

## Acceptance checklist → task map (docs/09)

| docs/09 item | Task |
| --- | --- |
| `CapturedEvent.timestampMillis` from an injectable clock (tested) | 2 |
| Events overlay off `OverlayLogger`; HTTP still works | 10 |
| Ordering answer implemented and tested | 3 |
| `useTimelineRail` exists, persisted, default false | 10 |
| Events module: switch, rail checkbox (hidden when off), View all, Clear; disabling clears | 10 |
| Release unaffected (noop) | 10, 14 |
| A: dot colours on glass | 4, 9 |
| A: chip size, padding, mono text, start ellipsis 330 dp | 9 |
| A: lifetime 5 s, hold, fade | 5, 9 |
| A: timer line drains and restarts | 5, 9 |
| A: coalescing ×N, restart, bump | 5, 9 |
| A: bursts 3/9 dp, 45 ms stagger | 5, 9 |
| A: no limit, fold pile + "+N older" | 5, 9 |
| A: enter/push/exit/fold animations A1–A9 | 9 |
| A: pass-through, no semantics | 10 |
| A: unit tests (slots, fold, coalescing, aging, burst) | 5 |
| B: strip, centre line, ticks, pulsing head | 6, 9b |
| B: marker shapes with white ring | 9b |
| B: 31 dp/s drift, removal, top fade | 6, 9b |
| B: labels 9 s, alpha, in/out B2/B3 | 6, 9b |
| B: push spacing 26/7 | 6, 9b |
| B: burst link | 6, 9b |
| B: screen bands | 6, 9b |
| B: leaders mode | dropped (push only, decided in brainstorming) |
| B: unit tests (push, bands, link, label alpha) | 6 |
| Screen: top bar, segmented, search, pinned copy | 12 |
| Timeline matches references | 12, 14 |
| Journey matches references | 12, 14 |
| Row rules (3 inline + more, expanded + timestamp, no-props) | 11 |
| Delta + gap markers | 2, 7, 11 |
| Session card with chip and duration | 11 |
| Journey bands, view last, duration/now, resumed, loose | 8, 11, 12 |
| Lanes card follows scroll; three lanes | 8, 11, 12 |
| Search both views | 7, 8, 12 |
| Copy JSON scope + clipboard + confirmation | 2, 12 |
| Empty state | 12 |
| Expansion survives config change | 12 |
| Viewer unit tests (delta, duration, gaps, journey, search, JSON) | 2, 7, 8 |
| Previews compared with references | 9, 9b, 11, 12, 14 |

---

### Task 1: Screenshot tests for `:debug-menu` with the generator plugin, in CI

**Files:**
- Modify: `build-logic/src/main/kotlin/ScreenshotTestsPlugin.kt`
- Modify: `debug-menu/build.gradle.kts`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/base/ScreenshotTest.kt`
- Create: `debug-menu/src/screenshotTest/AndroidManifest.xml`
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/base/PillSegmentedControl.kt` (first annotated preview)
- Modify: `.github/workflows/screenshot-tests.yml`, `.github/scripts/screenshot_report.py`, `.github/scripts/updated_references.sh`
- Modify: `CLAUDE.md` (Screenshot Testing section)

**Interfaces:**
- Produces: `com.sloy.debugmenu.base.ScreenshotTest(suite: ScreenshotSuite)`, `enum ScreenshotSuite { Components, Screens }`. Tasks 9–12 annotate previews with it. The generated classes are `com.sloy.debugmenu.ComponentsScreenshotTests` / `ScreensScreenshotTests`. Commands: `./gradlew :debug-menu:updateDebugScreenshotTest`, `:debug-menu:validateDebugScreenshotTest`.

- [ ] **Step 1: Make the plugin hook library modules too.** Replace `apply` in `ScreenshotTestsPlugin.kt`:

```kotlin
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasHostTests
import com.android.build.api.variant.HostTestBuilder
// remove the ApplicationAndroidComponentsExtension import

    override fun apply(project: Project) {
        listOf("com.android.application", "com.android.library").forEach { androidPlugin ->
            project.pluginManager.withPlugin(androidPlugin) {
                project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
                    val screenshotTest = (variant as? HasHostTests)?.hostTests?.get(HostTestBuilder.SCREENSHOT_TEST_TYPE)
                        ?: return@onVariants
                    project.registerTasks(variant.name, variant.namespace, screenshotTest.sources.kotlin)
                }
            }
        }
    }
```

Update the class KDoc's last sentence to "The generated test classes go in the module namespace." If `HasHostTests` doesn't exist in AGP 9.4's gradle-api, check `ApplicationVariant`/`LibraryVariant` for `hostTests` and cast to each instead. Run `./gradlew -p build-logic test`. Expected: PASS (the generator logic is unchanged).

- [ ] **Step 2: Add the annotation to debug-menu.** Create `base/ScreenshotTest.kt`:

```kotlin
package com.sloy.debugmenu.base

/**
 * Adds the annotated preview to the debug-menu screenshot tests.
 *
 * The `generate<Variant>ScreenshotTests` Gradle task reads the main sources and generates a `@PreviewTest` wrapper
 * calling the preview in the [suite] class. The test is named after the preview function.
 * The preview must be `internal` or `public` and have no parameters.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class ScreenshotTest(val suite: ScreenshotSuite)

enum class ScreenshotSuite {
    /** Reusable components and screen sections, generated in `ComponentsScreenshotTests`. */
    Components,

    /** Full screens and overlays, generated in `ScreensScreenshotTests`. */
    Screens,
}
```

- [ ] **Step 3: Apply the plugins to debug-menu.** In `debug-menu/build.gradle.kts`:

```kotlin
import com.android.compose.screenshot.tasks.PreviewScreenshotValidationTask

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.screenshot)
    id("sevibus.screenshot-tests")
}

android {
    // existing config …
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

tasks.withType<PreviewScreenshotValidationTask>().configureEach {
    testEngineInput.threshold.set(0.01f)
}

dependencies {
    // existing …
    screenshotTestImplementation(libs.compose.screenshot.validation)
    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.androidx.ui)
}
```

Copy `app/src/screenshotTest/AndroidManifest.xml` to `debug-menu/src/screenshotTest/AndroidManifest.xml`. If the screenshot plugin rejects library modules, stop and report: the fallback is to discuss before changing approach.

- [ ] **Step 4: Annotate the first preview.** In `PillSegmentedControl.kt`, rename `private fun PillSegmentedControlPreview` to `internal fun PillSegmentedControlTimelinePreview`, add `@ScreenshotTest(ScreenshotSuite.Components)` above `@PreviewLightDark`, and change its options to `listOf("Timeline", "Journey")` with `selectedIndex = 0`.

- [ ] **Step 5: Generate and validate.**

Run: `./gradlew :debug-menu:updateDebugScreenshotTest && ./gradlew :debug-menu:validateDebugScreenshotTest`
Expected: `debug-menu/src/screenshotTestDebug/reference/com/sloy/debugmenu/ComponentsScreenshotTests/PillSegmentedControlTimelinePreview_b2db1d68_0.png` and `_f6f1fda3_0.png` exist, and validation passes. Open both PNGs and compare them with `$HANDOFF/components/viewer-segmented-control.png`. Only the colours should differ (stock M3 vs SevTheme). Also run `./gradlew :app:validateDebugScreenshotTest`. Expected: PASS (the app is unaffected).

- [ ] **Step 6: Make the report script multi-module.** In `.github/scripts/screenshot_report.py`:
  - Replace the `REFERENCE_DIR`/`RENDERED_DIR` constants with functions `reference_dir(module)` → `f"{module}/src/screenshotTestDebug/reference/"` and `rendered_dir(module)` → `f"{module}/build/outputs/screenshotTest-results/preview/debug/rendered/"`.
  - Replace `--results-dir` with a repeatable `--module` (`action="append", required=True`). Each module's results are read from `f"{module}/build/test-results/validateDebugScreenshotTest"`.
  - Have `parse_failures(module)` add `"module": module` to each failure and use that module's dirs for the missing-reference rewrite.
  - Raise `SystemExit` only when **no** module produced results (a module that didn't run is skipped with a printed warning).
  - Use `test_id = f"{failure['module']}.{failure['test_class']}.{failure['test_name']}"` in `build_comment`, so image names don't collide.
  - Write `update-filters-<module>.txt` per module that has failures, with the same `--tests *.{class}.{name}` lines. Don't write a file for modules without failures.

- [ ] **Step 7: Run the script locally against fake results.**

```bash
mkdir -p /tmp/sr/app/build/test-results/validateDebugScreenshotTest /tmp/sr/debug-menu/build/test-results/validateDebugScreenshotTest
cat > /tmp/sr/debug-menu/build/test-results/validateDebugScreenshotTest/r.xml <<'EOF'
<testsuite><testcase classname="com.sloy.debugmenu.ComponentsScreenshotTests" name="FooPreview"><failure message="Reference image file does not exist (debug-menu/src/screenshotTestDebug/reference/x.png)"/></testcase></testsuite>
EOF
cat > /tmp/sr/app/build/test-results/validateDebugScreenshotTest/r.xml <<'EOF'
<testsuite><testcase classname="com.sloy.sevibus.ComponentsScreenshotTests" name="BarPreview"/></testsuite>
EOF
(cd /tmp/sr && python3 -I "$OLDPWD/.github/scripts/screenshot_report.py" --module app --module debug-menu --images-dir /tmp/sr/img --output-dir /tmp/sr/out --run-url http://x)
ls /tmp/sr/out; cat /tmp/sr/out/update-filters-debug-menu.txt
```

Expected: `1 failing screenshot test(s)`, `comment.md` mentions `debug-menu.ComponentsScreenshotTests.FooPreview`, only `update-filters-debug-menu.txt` exists and it contains `--tests *.ComponentsScreenshotTests.FooPreview`.

- [ ] **Step 8: Update the workflow and `updated_references.sh`.**
  - `updated_references.sh`: diff `-- app/src/screenshotTestDebug/reference debug-menu/src/screenshotTestDebug/reference`, and prefix `name` with the module (`${file%%/*}/…`).
  - Workflow:
    - "Validate screenshots": `./gradlew --continue :app:validateDebugScreenshotTest :debug-menu:validateDebugScreenshotTest`.
    - "Publish test results": `report_paths: '*/build/test-results/validateDebugScreenshotTest/*.xml'`.
    - "Build report": `--module app --module debug-menu` instead of `--results-dir`.
    - Both update steps: replace the single gradle call and `git add` with

      ```bash
      for module in app debug-menu; do
        filters="$REPORT_DIR/update-filters-$module.txt"
        if [ -s "$filters" ]; then ./gradlew ":$module:updateDebugScreenshotTest" $(cat "$filters"); fi
      done
      git add app/src/screenshotTestDebug/reference debug-menu/src/screenshotTestDebug/reference
      ```

    - "Upload screenshot report": add the `debug-menu/build/reports/screenshotTest/` and `debug-menu/build/outputs/screenshotTest-results/` paths.

  Validate the YAML: `python3 -I -c "import yaml,sys; yaml.safe_load(open('.github/workflows/screenshot-tests.yml'))"`. If PyYAML is missing, check the indentation by eye.

- [ ] **Step 9: Document it in CLAUDE.md.** In "Screenshot Testing" add a short paragraph:
  - `:debug-menu` also has screenshot tests through the same plugin, with its own `com.sloy.debugmenu.base.ScreenshotTest` annotation.
  - Its references live in `debug-menu/src/screenshotTestDebug/reference/`.
  - Its previews use `DebugPreviewTheme` and never SeviBus UI.
  - Commands use the `:debug-menu:` prefix.
  - CI covers both modules.

  Also change "registers it and the orphan tasks below. `app/build.gradle.kts` only applies it, and the generated classes go in the app namespace" to say that it applies to application and library modules, and the generated classes go in the module namespace.

- [ ] **Step 10: Commit.**

```bash
git add build-logic debug-menu/build.gradle.kts debug-menu/src/main/java/com/sloy/debugmenu/base debug-menu/src/screenshotTest debug-menu/src/screenshotTestDebug .github CLAUDE.md
git commit -m "test: screenshot tests for the debug menu module"
```

---

### Task 2: Millisecond timestamps, formatting and JSON export

**Files:**
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/CapturedEvent.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventFormat.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventsJson.kt`
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventLogScreen.kt` (temporarily: previews build `CapturedEvent` with `timestampMillis`, share uses `toJsonObject`). It's fully rewritten in Task 12.
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventOverlayLoggerItem.kt` (preview constructor only, deleted in Task 10)
- Modify: `app/src/debug/java/com/sloy/sevibus/feature/debug/events/OverlayTracker.kt`, `app/src/debug/java/com/sloy/sevibus/infrastructure/BuildVariantDI.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/EventFormatTest.kt` (new), `EventsJsonTest.kt` (new, replaces `CapturedEventTest.kt`), `EventStoreTest.kt` (helper), `app/src/testDebug/java/com/sloy/sevibus/feature/debug/events/OverlayTrackerTest.kt`

**Interfaces:**
- Produces:
  - `data class CapturedEvent(name: String, properties: Map<String,String> = emptyMap(), timestampMillis: Long, id: String = UUID…)`, with `val timestamp: String` (HH:mm:ss, system zone) and `val type: EventType`.
  - `internal fun List<CapturedEvent>.chronological(): List<CapturedEvent>` (oldest first, stable for equal timestamps given a newest-first input).
  - `internal fun Long.toClockTime(zone: ZoneId = ZoneId.systemDefault()): String`, `internal fun Long.toClockTimeMillis(zone: ZoneId = ZoneId.systemDefault()): String`.
  - `internal fun formatDuration(millis: Long): String`, `internal fun formatSeconds(seconds: Long): String`, `internal fun formatDelta(millis: Long): String`.
  - `internal fun CapturedEvent.toJsonObject(): JsonObject`, `internal fun List<CapturedEvent>.toSessionJson(): String`.
  - `class OverlayTracker(eventStore: EventStore, clock: () -> Long = System::currentTimeMillis)`.

- [ ] **Step 1: Write the failing tests.** `EventFormatTest.kt`:

```kotlin
package com.sloy.debugmenu.events

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDateTime
import java.time.ZoneOffset

class EventFormatTest {

    private val millis = LocalDateTime.of(2026, 10, 8, 16, 21, 8, 100_000_000).toInstant(ZoneOffset.UTC).toEpochMilli()

    @Test
    fun `clock time has seconds`() {
        expectThat(millis.toClockTime(ZoneOffset.UTC)).isEqualTo("16:21:08")
    }

    @Test
    fun `clock time with millis`() {
        expectThat(millis.toClockTimeMillis(ZoneOffset.UTC)).isEqualTo("16:21:08.100")
    }

    @Test
    fun `durations under 10 seconds have one decimal`() {
        expectThat(formatDuration(3_385)).isEqualTo("3.4s")
        expectThat(formatDuration(1_280)).isEqualTo("1.3s")
    }

    @Test
    fun `durations under a minute are whole seconds`() {
        expectThat(formatDuration(14_000)).isEqualTo("14s")
    }

    @Test
    fun `durations of a minute or more have minutes and seconds`() {
        expectThat(formatDuration(132_580)).isEqualTo("2m 13s")
        expectThat(formatDuration(94_000)).isEqualTo("1m 34s")
    }

    @Test
    fun `durations never show 60 seconds`() {
        expectThat(formatDuration(59_700)).isEqualTo("1m 0s")
        expectThat(formatDuration(119_600)).isEqualTo("2m 0s")
    }

    @Test
    fun `whole seconds`() {
        expectThat(formatSeconds(14)).isEqualTo("14s")
        expectThat(formatSeconds(97)).isEqualTo("1m 37s")
    }

    @Test
    fun `delta under a minute has two decimals`() {
        expectThat(formatDelta(650)).isEqualTo("+0.65s")
        expectThat(formatDelta(15)).isEqualTo("+0.02s")
    }

    @Test
    fun `delta of a minute or more uses minutes`() {
        expectThat(formatDelta(132_580)).isEqualTo("+2m 13s")
    }

    @Test
    fun `chronological sorts by timestamp and keeps insertion order for ties`() {
        val newestFirst = listOf(
            CapturedEvent("C", timestampMillis = 20, id = "c"),
            CapturedEvent("B", timestampMillis = 10, id = "b"),
            CapturedEvent("A", timestampMillis = 10, id = "a"),
        )
        expectThat(newestFirst.chronological().map { it.id }).isEqualTo(listOf("a", "b", "c"))
    }
}
```

`formatDelta` rounds half-up with exact decimal arithmetic (`BigDecimal`), so 15 ms is `+0.02s` and 645 ms is `+0.65s`, as in the reference screenshots.

`EventsJsonTest.kt` (delete `CapturedEventTest.kt`):

```kotlin
package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

class EventsJsonTest {

    @Test
    fun `exports oldest first with millis and properties`() {
        val newestFirst = listOf(
            CapturedEvent("For You Viewed", mapOf("trigger" to "launch"), timestampMillis = 1_120, id = "2"),
            CapturedEvent("App Started", timestampMillis = 1_050, id = "1"),
        )

        val array = Json.parseToJsonElement(newestFirst.toSessionJson()).jsonArray

        expectThat(array.map { it.jsonObject["name"]!!.jsonPrimitive.content }).containsExactly("App Started", "For You Viewed")
        expectThat(array[0].jsonObject["timestampMillis"]!!.jsonPrimitive.long).isEqualTo(1_050)
        expectThat(array[1].jsonObject["properties"]!!.jsonObject["trigger"]!!.jsonPrimitive.content).isEqualTo("launch")
        expectThat(array[1].jsonObject["timestamp"]!!.jsonPrimitive.content).isEqualTo(1_120L.toClockTime())
    }

    @Test
    fun `empty store exports an empty array`() {
        expectThat(Json.parseToJsonElement(emptyList<CapturedEvent>().toSessionJson()).jsonArray.size).isEqualTo(0)
    }
}
```

In `EventStoreTest.kt` change the helper to `CapturedEvent(name = name, timestampMillis = 0)`.

Rewrite `OverlayTrackerTest.kt`:

```kotlin
class OverlayTrackerTest {

    private val eventStore = EventStore()
    private val tracker = OverlayTracker(eventStore, clock = { 1_791_555_661_050 })

    @Test
    fun `maps event name properties and timestamp`() {
        tracker.track(TestEvent)

        expectThat(eventStore.events.value).single().and {
            get { name }.isEqualTo("Test Clicked")
            get { properties }.isEqualTo(mapOf("stopId" to "42", "isSelected" to "true"))
            get { timestampMillis }.isEqualTo(1_791_555_661_050)
        }
    }

    private object TestEvent : SevEvent("Test Clicked", "stopId" to 42, "isSelected" to true, "missing" to null)
}
```

(Remove the overlay tests and the now-unused imports. The overlay no longer goes through the tracker.)

- [ ] **Step 2: Run them and check they fail.** Run `./gradlew :debug-menu:testDebugUnitTest :app:testDebugUnitTest --tests '*OverlayTrackerTest'`. Expected: compilation FAIL (`timestampMillis`, `formatDuration` … unresolved).

- [ ] **Step 3: Implement.** `CapturedEvent.kt`:

```kotlin
package com.sloy.debugmenu.events

import java.util.UUID

/**
 * Analytics event captured for debugging purposes.
 */
data class CapturedEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
    val timestampMillis: Long,
    val id: String = UUID.randomUUID().toString(),
) {
    val timestamp: String get() = timestampMillis.toClockTime()
    val type: EventType get() = EventType.of(name)
}

internal fun List<CapturedEvent>.chronological(): List<CapturedEvent> = asReversed().sortedBy { it.timestampMillis }
```

`EventFormat.kt`:

```kotlin
package com.sloy.debugmenu.events

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
private val CLOCK_MILLIS: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

internal fun Long.toClockTime(zone: ZoneId = ZoneId.systemDefault()): String = Instant.ofEpochMilli(this).atZone(zone).format(CLOCK)

internal fun Long.toClockTimeMillis(zone: ZoneId = ZoneId.systemDefault()): String =
    Instant.ofEpochMilli(this).atZone(zone).format(CLOCK_MILLIS)

internal fun formatDuration(millis: Long): String {
    val tenths = (millis + 50) / 100
    if (tenths < 100) return String.format(Locale.US, "%.1fs", tenths / 10.0)
    return formatSeconds((millis + 500) / 1000)
}

internal fun formatSeconds(seconds: Long): String =
    if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"

internal fun formatDelta(millis: Long): String =
    if (millis < 59_995) "+${BigDecimal.valueOf(millis, 3).setScale(2, RoundingMode.HALF_UP).toPlainString()}s"
    else "+${formatDuration(millis)}"
```

`EventsJson.kt`:

```kotlin
package com.sloy.debugmenu.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

internal fun CapturedEvent.toJsonObject(): JsonObject = buildJsonObject {
    put("name", name)
    put("timestamp", timestamp)
    put("timestampMillis", timestampMillis)
    putJsonObject("properties") {
        properties.forEach { (key, value) -> put(key, value) }
    }
}

internal fun List<CapturedEvent>.toSessionJson(): String =
    PrettyJson.encodeToString(JsonArray.serializer(), JsonArray(chronological().map { it.toJsonObject() }))

private val PrettyJson = Json { prettyPrint = true }
```

In `EventLogScreen.kt` replace `event.toPrettyJson()` with `PrettyEventJson.encodeToString(JsonObject.serializer(), event.toJsonObject())`, or simply `event.toJsonObject().toString()`. This screen is replaced in Task 12, so use `event.toJsonObject().toString()`. Update its previews to `CapturedEvent("…", mapOf(…), timestampMillis = 0, id = "1")`. Update the `EventOverlayLoggerItem` preview the same way.

`OverlayTracker.kt`:

```kotlin
class OverlayTracker(
    private val eventStore: EventStore,
    private val clock: () -> Long = System::currentTimeMillis,
) : Tracker {
    override fun track(event: SevEvent) {
        eventStore.add(event.toCapturedEvent(clock()))
    }
}

internal fun SevEvent.toCapturedEvent(timestampMillis: Long): CapturedEvent = CapturedEvent(
    name = name,
    properties = properties.mapNotNull { (key, value) -> value?.let { key to it.toString() } }.toMap(),
    timestampMillis = timestampMillis,
)
```

In `BuildVariantDI.kt`: `single { OverlayTracker(get()) }.bind(Tracker::class)`. The overlay chips stop until Task 10 wires the new overlay; that's expected on this branch.

- [ ] **Step 4: Run the tests and check they pass.** Run `./gradlew :debug-menu:testDebugUnitTest :app:testDebugUnitTest --tests '*OverlayTrackerTest'`. Expected: PASS.

- [ ] **Step 5: Commit.** `git commit -am "refactor: millisecond timestamps for captured events"` (add the new files first).

---

### Task 3: Analytics delivers events to trackers in call order

**Files:**
- Modify: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/Analytics.kt`
- Test: `app/src/test/java/com/sloy/sevibus/infrastructure/analytics/AnalyticsTest.kt` (new)

**Interfaces:**
- Produces: `class Analytics(trackers: List<Tracker>, analyticsSettingsDataSource: AnalyticsSettingsDataSource, scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()))`. Its public API (`track`, `setUserProperty`) is unchanged.

- [ ] **Step 1: Write the failing test.**

```kotlin
package com.sloy.sevibus.infrastructure.analytics

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.onBlocking
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class AnalyticsTest {

    private val tracked = mutableListOf<String>()
    private val tracker = object : Tracker {
        override fun track(event: SevEvent) {
            tracked += event.name
        }
    }

    @Test
    fun `trackers receive events in call order when the enabled check suspends`() = runTest {
        var checks = 0
        val settings = mock<AnalyticsSettingsDataSource> {
            onBlocking { isAnalyticsEnabled() } doSuspendableAnswer {
                delay(if (checks++ == 0) 100 else 0)
                true
            }
        }
        val analytics = Analytics(listOf(tracker), settings, backgroundScope)

        analytics.track(TestEvent("First"))
        analytics.track(TestEvent("Second"))
        advanceUntilIdle()

        expectThat(tracked).containsExactly("First", "Second")
    }

    @Test
    fun `nothing is tracked when analytics is disabled`() = runTest {
        val settings = mock<AnalyticsSettingsDataSource> {
            onBlocking { isAnalyticsEnabled() } doSuspendableAnswer { false }
        }
        val analytics = Analytics(listOf(tracker), settings, backgroundScope)

        analytics.track(TestEvent("First"))
        advanceUntilIdle()

        expectThat(tracked).isEmpty()
    }

    private class TestEvent(name: String) : SevEvent(name)
}
```

- [ ] **Step 2: Run the test and check it fails.** Run `./gradlew :app:testDebugUnitTest --tests '*.AnalyticsTest'`. Expected: compilation FAIL (no `scope` parameter). After adding only the parameter to the current implementation (`scope.launch` per event), the first test fails with `Second, First`.

- [ ] **Step 3: Implement.**

```kotlin
class Analytics(
    private val trackers: List<Tracker>,
    private val analyticsSettingsDataSource: AnalyticsSettingsDataSource,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) {
    private val dispatches = Channel<() -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (dispatch in dispatches) {
                if (analyticsSettingsDataSource.isAnalyticsEnabled()) dispatch()
            }
        }
    }

    fun track(event: SevEvent) {
        dispatches.trySend { trackers.forEach { it.track(event) } }
    }

    fun setUserProperty(property: UserProperty) {
        dispatches.trySend { trackers.forEach { it.setUserProperty(property) } }
    }
}
```

Imports: `kotlinx.coroutines.channels.Channel`. `limitedParallelism(1)` isn't enough: the coroutine suspends in `isAnalyticsEnabled()` and the next one runs. That's why this uses a single consumer of a queue. Check that `grep -rn "Analytics(" app/src/main` still compiles with the default `scope`.

- [ ] **Step 4: Run the tests.** Run `./gradlew :app:testDebugUnitTest --tests '*.analytics.*'`. Expected: PASS (`SessionTracker`/`UserPropertiesTracker` tests included).

- [ ] **Step 5: Commit.** `git commit -m "fix: deliver analytics events to trackers in call order"`

---

### Task 4: Event accent tokens and overlay style

**Files:**
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventType.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventText.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/OverlayStyle.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/EventTypeTest.kt` (extend)

**Interfaces:**
- Produces:
  - `enum class EventType(color, onDark, ink, inkOnDark, tintAlpha, tintAlphaOnDark)`.
  - `internal data class EventColors(accent: Color, tint: Color, ink: Color)`, `internal fun EventType.colors(dark: Boolean): EventColors`, `@Composable internal fun EventType.colors(): EventColors`, `@Composable internal fun isDarkSurface(): Boolean`.
  - `internal fun screenBandColor(dark: Boolean): Color`.
  - `internal object EventText { Mono12, Mono12Medium, Mono11, Mono11Medium, Mono10, Mono10Medium, Mono9, Mono9Medium }`.
  - `internal object OverlayColors {…}` and `internal object OverlayEasing { SpringyOut, PopOut, LabelOut, ExitAccel, CssEaseIn, CssEaseOut, CssEase }`.
  - `EventTypeIcon` stays until Task 10/12 delete its last users. Delete it in Task 12.

- [ ] **Step 1: Write the failing test.** Append to `EventTypeTest`:

```kotlin
    @Test
    fun `light colors use the base accent and its tint`() {
        val colors = EventType.VIEW.colors(dark = false)
        expectThat(colors.accent).isEqualTo(Color(0xFF3F51B5))
        expectThat(colors.tint).isEqualTo(Color(0xFF3F51B5).copy(alpha = 0.16f))
        expectThat(colors.ink).isEqualTo(Color(0xFF2F3D8F))
    }

    @Test
    fun `dark colors use the on dark accent`() {
        val colors = EventType.OTHER.colors(dark = true)
        expectThat(colors.accent).isEqualTo(Color(0xFFFFB547))
        expectThat(colors.tint).isEqualTo(Color(0xFFFFB547).copy(alpha = 0.18f))
        expectThat(colors.ink).isEqualTo(Color(0xFFFFC977))
    }

    @Test
    fun `click on dark is green`() {
        expectThat(EventType.CLICK.colors(dark = true).accent).isEqualTo(Color(0xFF7BD67F))
    }
```

- [ ] **Step 2: Run and check it fails.** Run `./gradlew :debug-menu:testDebugUnitTest --tests '*EventTypeTest'`. Expected: FAIL (unresolved `colors`).

- [ ] **Step 3: Implement.** `EventType.kt`:

```kotlin
enum class EventType(
    val color: Color,
    val onDark: Color,
    val ink: Color,
    val inkOnDark: Color,
    val tintAlpha: Float,
    val tintAlphaOnDark: Float,
) {
    CLICK(Color(0xFF4CAF50), Color(0xFF7BD67F), Color(0xFF2E7D32), Color(0xFF7BD67F), 0.16f, 0.18f),
    VIEW(Color(0xFF3F51B5), Color(0xFF8C9BFF), Color(0xFF2F3D8F), Color(0xFFB4BEFF), 0.16f, 0.20f),
    OTHER(Color(0xFFFFA726), Color(0xFFFFB547), Color(0xFF8A4B00), Color(0xFFFFC977), 0.18f, 0.18f);

    companion object { /* of() unchanged */ }
}

@Immutable
internal data class EventColors(val accent: Color, val tint: Color, val ink: Color)

internal fun EventType.colors(dark: Boolean): EventColors =
    if (dark) EventColors(onDark, onDark.copy(alpha = tintAlphaOnDark), inkOnDark)
    else EventColors(color, color.copy(alpha = tintAlpha), ink)

@Composable
@ReadOnlyComposable
internal fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
@ReadOnlyComposable
internal fun EventType.colors(): EventColors = colors(isDarkSurface())

internal fun screenBandColor(dark: Boolean): Color =
    if (dark) EventType.VIEW.onDark.copy(alpha = 0.07f) else EventType.VIEW.color.copy(alpha = 0.06f)
```

`EventText.kt`:

```kotlin
package com.sloy.debugmenu.events

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal object EventText {
    val Mono12 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp)
    val Mono12Medium = Mono12.copy(fontWeight = FontWeight.Medium)
    val Mono11 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, lineHeight = 16.sp)
    val Mono11Medium = Mono11.copy(fontWeight = FontWeight.Medium)
    val Mono10 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp, lineHeight = 14.sp)
    val Mono10Medium = Mono10.copy(fontWeight = FontWeight.Medium)
    val Mono9 = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, lineHeight = 12.sp)
    val Mono9Medium = Mono9.copy(fontWeight = FontWeight.Medium)
}
```

`overlay/OverlayStyle.kt`:

```kotlin
package com.sloy.debugmenu.events.overlay

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF14151A)

internal object OverlayColors {
    val Glass = Ink.copy(alpha = 0.72f)
    val GlassMuted = Ink.copy(alpha = 0.5f)
    val OnGlass = Color.White
    val Badge = Color.White.copy(alpha = 0.16f)
    val TimerTrack = Color.White.copy(alpha = 0.08f)
    val RailTrack = Color.White.copy(alpha = 0.55f)
    val RailTrackBorder = Ink.copy(alpha = 0.08f)
    val RailLine = Ink.copy(alpha = 0.22f)
    val RailTick = Ink.copy(alpha = 0.35f)
    val RailTickText = Color(0xFF6B7080)
    val RailLink = Ink.copy(alpha = 0.55f)
    val MarkerRing = Color.White
    val MarkerShadow = Color.Black.copy(alpha = 0.3f)
    val RailBandFill = com.sloy.debugmenu.events.EventType.VIEW.color.copy(alpha = 0.13f)
    val RailBandBorder = com.sloy.debugmenu.events.EventType.VIEW.color.copy(alpha = 0.38f)
}

internal object OverlayEasing {
    val SpringyOut = CubicBezierEasing(0.30f, 1.35f, 0.45f, 1f)
    val PopOut = CubicBezierEasing(0.30f, 1.70f, 0.50f, 1f)
    val LabelOut = CubicBezierEasing(0.30f, 1.50f, 0.50f, 1f)
    val ExitAccel = CubicBezierEasing(0.30f, 0f, 0.80f, 0.15f)
    val CssEaseIn = CubicBezierEasing(0.42f, 0f, 1f, 1f)
    val CssEaseOut = CubicBezierEasing(0f, 0f, 0.58f, 1f)
    val CssEase = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
}
```

(Import `EventType` normally instead of the fully qualified names.) `CubicBezierEasing` accepts y > 1. If it throws at runtime, use `spring(dampingRatio = 0.6f, stiffness = 300f)` for the transforms (docs/08).

- [ ] **Step 4: Run the tests.** Expected: PASS.
- [ ] **Step 5: Commit.** `git commit -m "feat(debug): event accent tokens and overlay style"`

---

### Task 5: Stack layout (pure)

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/StackLayout.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/OverlayDemo.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/overlay/StackLayoutTest.kt`

**Interfaces:**
- Consumes: `CapturedEvent.chronological()`, `CapturedEvent.type` (Task 2).
- Produces:
  - `internal object StackSpec` (constants below).
  - `internal enum class ChipPhase { IN, OUT }`.
  - `internal data class StackChip(key: String, name: String, type: EventType, count: Int, phase: ChipPhase, y: Float, scale: Float, alpha: Float, timerFraction: Float, foldIndex: Int?)`.
  - `internal data class StackFrame(chips: List<StackChip> /* newest first */, olderCount: Int, olderY: Float)`.
  - `internal fun stackFrame(events: List<CapturedEvent>, nowMillis: Long, maxHeight: Float, lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS): StackFrame`.
  - `internal fun stackAlpha(ageMillis: Long, lifetimeMillis: Long): Float`.
  - `internal object OverlayDemo { fun events(startMillis: Long): List<CapturedEvent> /* newest first */ }`.
  - `y` is in dp: 0 is the stack bottom and negative values go up. Every chip is bottom-anchored.

- [ ] **Step 1: Write the demo data.** `OverlayDemo.kt` (docs/03 sequence, one loop, events of a batch share a timestamp):

```kotlin
package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent

internal object OverlayDemo {
    private val BATCHES: List<Pair<Long, List<String>>> = listOf(
        700L to listOf("Lines Viewed", "Line Paths Displayed"),
        2_500L to listOf("Map Explored"),
        3_400L to listOf("Map Stop Clicked", "Stop Details Viewed", "Bottom Sheet Changed", "Arrivals Displayed"),
        5_800L to listOf("Arrivals Displayed"),
        6_500L to listOf("Bottom Sheet Changed", "Stop Details Closed"),
        8_000L to listOf("Edit Favorites Clicked", "Edit Favorites Viewed"),
        10_000L to listOf("Edit Favorites Cancelled", "For You Viewed", "Arrivals Displayed", "Arrivals Displayed", "Arrivals Displayed"),
        11_200L to listOf("Favorite Stop Clicked", "Stop Details Viewed"),
        11_700L to listOf("Arrivals Displayed"),
        14_300L to listOf("Lines Viewed", "Line Paths Displayed"),
    )

    fun events(startMillis: Long): List<CapturedEvent> =
        BATCHES.flatMap { (at, names) ->
            names.mapIndexed { index, name -> CapturedEvent(name, timestampMillis = startMillis + at, id = "demo-$at-$index") }
        }.asReversed()
}
```

- [ ] **Step 2: Write the failing tests.** `StackLayoutTest.kt`:

```kotlin
package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import strikt.assertions.single

class StackLayoutTest {

    private val tall = 560f

    private fun events(vararg nameAndTime: Pair<String, Long>): List<CapturedEvent> =
        nameAndTime.mapIndexed { index, (name, at) -> CapturedEvent(name, timestampMillis = at, id = "$index") }.asReversed()

    @Test
    fun `empty events give an empty frame`() {
        expectThat(stackFrame(emptyList(), 1_000, tall)).isEqualTo(StackFrame(emptyList(), 0, -34f))
    }

    @Test
    fun `a fresh chip sits at the bottom fully visible`() {
        val chip = stackFrame(events("Lines Viewed" to 0), 0, tall).chips.single()
        expectThat(chip.y).isEqualTo(0f)
        expectThat(chip.alpha).isEqualTo(1f)
        expectThat(chip.timerFraction).isEqualTo(1f)
        expectThat(chip.phase).isEqualTo(ChipPhase.IN)
        expectThat(chip.foldIndex).isNull()
    }

    @Test
    fun `newest is at the bottom and separate events are 9dp apart`() {
        val frame = stackFrame(events("A" to 0, "B" to 1_000), 1_000, tall)
        expectThat(frame.chips.map { it.name to it.y }).containsExactly("B" to 0f, "A" to -37f)
    }

    @Test
    fun `events within 150ms are a burst 3dp apart`() {
        val frame = stackFrame(events("A" to 0, "B" to 150), 1_000, tall)
        expectThat(frame.chips.map { it.y }).containsExactly(0f, -31f)
    }

    @Test
    fun `same timestamp events enter 45ms apart`() {
        val batch = events("A" to 1_000, "B" to 1_000)
        expectThat(stackFrame(batch, 1_044, tall).chips.map { it.name }).containsExactly("A")
        expectThat(stackFrame(batch, 1_045, tall).chips.map { it.name }).containsExactly("B", "A")
    }

    @Test
    fun `future events are not shown yet`() {
        expectThat(stackFrame(events("A" to 2_000), 1_000, tall).chips).isEmpty()
    }

    @Test
    fun `repeating the newest name within 3s coalesces and restarts the lifetime`() {
        val frame = stackFrame(events("A" to 0, "A" to 2_000), 2_000, tall)
        expectThat(frame.chips).single().and {
            get { count }.isEqualTo(2)
            get { timerFraction }.isEqualTo(1f)
            get { alpha }.isEqualTo(1f)
        }
    }

    @Test
    fun `repeating after 3s gives a new chip`() {
        expectThat(stackFrame(events("A" to 0, "A" to 3_000), 3_000, tall).chips.map { it.count }).containsExactly(1, 1)
    }

    @Test
    fun `only the newest chip coalesces`() {
        val frame = stackFrame(events("A" to 0, "B" to 500, "A" to 1_000), 1_000, tall)
        expectThat(frame.chips.map { it.name }).containsExactly("A", "B", "A")
    }

    @Test
    fun `alpha holds for 2s then fades to half at the end of the lifetime`() {
        expectThat(stackAlpha(2_000, 5_000)).isEqualTo(1f)
        expectThat(stackAlpha(3_500, 5_000)).isEqualTo(0.75f)
        expectThat(stackAlpha(5_000, 5_000)).isEqualTo(0.5f)
    }

    @Test
    fun `timer drains over the lifetime`() {
        expectThat(stackFrame(events("A" to 0), 2_500, tall).chips.single().timerFraction).isEqualTo(0.5f)
    }

    @Test
    fun `a chip leaves after its lifetime and is removed 450ms later`() {
        val batch = events("A" to 0)
        expectThat(stackFrame(batch, 5_001, tall).chips.single()).and {
            get { phase }.isEqualTo(ChipPhase.OUT)
            get { alpha }.isEqualTo(0f)
        }
        expectThat(stackFrame(batch, 5_451, tall).chips).isEmpty()
    }

    @Test
    fun `chips beyond the available height fold into a pile`() {
        val batch = events("A" to 0, "B" to 1_000, "C" to 2_000, "D" to 3_000, "E" to 4_000)
        val frame = stackFrame(batch, 4_000, maxHeight = 28f + 37f + 37f)
        expectThat(frame.chips.map { it.name to it.foldIndex }).containsExactly("E" to null, "D" to null, "C" to null, "B" to 0, "A" to 1)
        expectThat(frame.chips[3]).and {
            get { y }.isEqualTo(-74f - 5f)
            get { scale }.isEqualTo(0.94f)
            get { alpha }.isEqualTo(0.40f)
        }
        expectThat(frame.chips[4]).and {
            get { y }.isEqualTo(-74f - 8f)
            get { scale }.isEqualTo(0.91f)
            get { alpha }.isEqualTo(0.28f)
        }
        expectThat(frame.olderCount).isEqualTo(2)
        expectThat(frame.olderY).isEqualTo(-74f - 34f)
    }

    @Test
    fun `only three folded chips peek out`() {
        val batch = events(*Array(6) { "E$it" to it * 1_000L })
        val frame = stackFrame(batch, 5_000, maxHeight = 28f)
        expectThat(frame.chips.drop(1).map { it.alpha }).containsExactly(0.40f, 0.28f, 0.16f, 0f, 0f)
        expectThat(frame.chips.drop(1).map { it.y }).containsExactly(-5f, -8f, -11f, -11f, -11f)
    }

    @Test
    fun `demo at 10_6s matches the reference stack`() {
        val frame = stackFrame(OverlayDemo.events(0), 10_600, tall)
        expectThat(frame.chips.map { it.name to it.count }).containsExactly(
            "Arrivals Displayed" to 3,
            "For You Viewed" to 1,
            "Edit Favorites Cancelled" to 1,
            "Edit Favorites Viewed" to 1,
            "Edit Favorites Clicked" to 1,
            "Stop Details Closed" to 1,
            "Bottom Sheet Changed" to 1,
            "Arrivals Displayed" to 2,
        )
        expectThat(frame.chips.map { it.y }).containsExactly(0f, -31f, -62f, -99f, -130f, -167f, -198f, -235f)
    }
}
```

Float equality: if any assertion fails by rounding (`0.28f` vs `0.28000003f`), compare with `isEqualTo(x, 0.0001)` from `strikt.assertions` for doubles, i.e. `get { alpha.toDouble() }.isEqualTo(0.28, 0.0001)`. Prefer making the implementation compute the constants exactly (see below).

- [ ] **Step 3: Run and check they fail.** Run `./gradlew :debug-menu:testDebugUnitTest --tests '*StackLayoutTest'`. Expected: compilation FAIL.

- [ ] **Step 4: Implement `StackLayout.kt`.**

```kotlin
package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological

internal object StackSpec {
    const val LIFETIME_MILLIS = 5_000L
    const val HOLD_MILLIS = 2_000L
    const val FADE_TO = 0.5f
    const val EXIT_MILLIS = 450L
    const val COALESCE_WINDOW_MILLIS = 3_000L
    const val BURST_WINDOW_MILLIS = 150L
    const val STAGGER_MILLIS = 45L
    const val CHIP_HEIGHT = 28f
    const val GAP_BURST = 3f
    const val GAP = 9f
    const val TOP_RESERVE = 160f
    const val OLDER_PILL_OFFSET = 34f
    private val FOLD_ALPHAS = floatArrayOf(0.40f, 0.28f, 0.16f)
    private val FOLD_SCALES = floatArrayOf(0.94f, 0.91f, 0.88f)

    fun foldAlpha(index: Int): Float = FOLD_ALPHAS.getOrElse(index) { 0f }
    fun foldScale(index: Int): Float = FOLD_SCALES[minOf(index, 2)]
    fun foldOffset(index: Int): Float = 5f + minOf(index, 2) * 3f
}

internal enum class ChipPhase { IN, OUT }

internal data class StackChip(
    val key: String,
    val name: String,
    val type: EventType,
    val count: Int,
    val phase: ChipPhase,
    val y: Float,
    val scale: Float,
    val alpha: Float,
    val timerFraction: Float,
    val foldIndex: Int?,
)

internal data class StackFrame(val chips: List<StackChip>, val olderCount: Int, val olderY: Float)

private class Run(val key: String, val name: String, val type: EventType, val firstAt: Long, var lastAt: Long, var count: Int = 1)

internal fun stackFrame(
    events: List<CapturedEvent>,
    nowMillis: Long,
    maxHeight: Float,
    lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS,
): StackFrame {
    val runs = buildRuns(events, nowMillis)
    val shown = runs.filter { nowMillis - it.lastAt <= lifetimeMillis + StackSpec.EXIT_MILLIS }.asReversed()

    val placed = ArrayList<StackChip>(shown.size)
    var y = 0f
    var topY = 0f
    var folded = 0
    shown.forEachIndexed { index, run ->
        if (index > 0) {
            val newer = shown[index - 1]
            val isBurst = newer.firstAt - run.lastAt <= StackSpec.BURST_WINDOW_MILLIS
            y -= StackSpec.CHIP_HEIGHT + if (isBurst) StackSpec.GAP_BURST else StackSpec.GAP
        }
        val foldIndex = if (folded > 0 || -y > maxHeight - StackSpec.CHIP_HEIGHT) folded++ else null
        if (foldIndex == null) topY = y
        val age = nowMillis - run.lastAt
        val phase = if (age <= lifetimeMillis) ChipPhase.IN else ChipPhase.OUT
        placed += StackChip(
            key = run.key,
            name = run.name,
            type = run.type,
            count = run.count,
            phase = phase,
            y = y,
            scale = 1f,
            alpha = if (phase == ChipPhase.OUT) 0f else stackAlpha(age, lifetimeMillis),
            timerFraction = (1f - age.toFloat() / lifetimeMillis).coerceIn(0f, 1f),
            foldIndex = foldIndex,
        )
    }
    val chips = placed.map { chip ->
        val foldIndex = chip.foldIndex ?: return@map chip
        chip.copy(
            y = topY - StackSpec.foldOffset(foldIndex),
            scale = StackSpec.foldScale(foldIndex),
            alpha = if (chip.phase == ChipPhase.OUT) 0f else StackSpec.foldAlpha(foldIndex),
        )
    }
    return StackFrame(chips, folded, topY - StackSpec.OLDER_PILL_OFFSET)
}

private fun buildRuns(events: List<CapturedEvent>, nowMillis: Long): List<Run> {
    val runs = mutableListOf<Run>()
    var previousEnterAt: Long? = null
    for (event in events.chronological()) {
        val enterAt = previousEnterAt?.let { maxOf(event.timestampMillis, it + StackSpec.STAGGER_MILLIS) } ?: event.timestampMillis
        if (enterAt > nowMillis) break
        previousEnterAt = enterAt
        val newest = runs.lastOrNull()
        if (newest != null && newest.name == event.name && enterAt - newest.lastAt < StackSpec.COALESCE_WINDOW_MILLIS) {
            newest.count++
            newest.lastAt = enterAt
        } else {
            runs += Run(event.id, event.name, event.type, enterAt, enterAt)
        }
    }
    return runs
}

internal fun stackAlpha(ageMillis: Long, lifetimeMillis: Long): Float {
    val hold = minOf(StackSpec.HOLD_MILLIS, lifetimeMillis - 500)
    if (ageMillis <= hold) return 1f
    val progress = ((ageMillis - hold).toFloat() / (lifetimeMillis - hold)).coerceAtMost(1f)
    return 1f - (1f - StackSpec.FADE_TO) * progress
}
```

- [ ] **Step 5: Run the tests.** Expected: PASS. If `demo at 10_6s` disagrees on y, recompute by hand from docs/05 and fix the implementation, not the expected values (they come from the reference screenshot).
- [ ] **Step 6: Commit.** `git commit -m "feat(debug): stack overlay layout"`

---

### Task 6: Rail layout (pure)

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/RailLayout.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/overlay/RailLayoutTest.kt`

**Interfaces:**
- Consumes: `chronological()`, `CapturedEvent.type`, `OverlayDemo.events` (Task 5).
- Produces:
  - `internal object RailSpec`.
  - `internal data class RailMarker(key: String, name: String, type: EventType, y: Float, trueY: Float, labelVisible: Boolean, labelAlpha: Float, markerAlpha: Float, linkLength: Float, popAt: Long)`.
  - `internal data class RailBand(key: String, top: Float, bottom: Float, alpha: Float)`, `internal data class RailTick(y: Float, label: String)`.
  - `internal data class RailFrame(markers: List<RailMarker> /* newest first */, bands: List<RailBand>, ticks: List<RailTick>)`.
  - `internal fun railFrame(events: List<CapturedEvent>, nowMillis: Long): RailFrame`, `internal fun railLabelAlpha(ageSeconds: Float): Float`.
  - `y` is in dp above the now head. `linkLength` is the length of the bar drawn from this marker down to its newer neighbour (0 = no link). Band `top > bottom`, both in y units, already extended by 10 dp.

- [ ] **Step 1: Write the failing tests.**

```kotlin
package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class RailLayoutTest {

    private fun events(vararg nameAndTime: Pair<String, Long>): List<CapturedEvent> =
        nameAndTime.mapIndexed { index, (name, at) -> CapturedEvent(name, timestampMillis = at, id = "$index") }.asReversed()

    @Test
    fun `markers drift up at 31dp per second`() {
        val marker = railFrame(events("A" to 0), 2_000).markers.single()
        expectThat(marker.y).isEqualTo(62f)
        expectThat(marker.trueY).isEqualTo(62f)
    }

    @Test
    fun `labelled neighbours are pushed 26dp apart`() {
        val frame = railFrame(events("A" to 1_000, "B" to 1_000), 1_000)
        expectThat(frame.markers.map { it.y }).containsExactly(0f, 26f)
    }

    @Test
    fun `unlabelled neighbours are pushed 7dp apart`() {
        val frame = railFrame(events("A" to 0, "B" to 0), 10_000)
        expectThat(frame.markers.map { it.y }).containsExactly(310f, 317f)
    }

    @Test
    fun `spread events keep their true position`() {
        val frame = railFrame(events("A" to 0, "B" to 2_000), 2_000)
        expectThat(frame.markers.map { it.y }).containsExactly(0f, 62f)
    }

    @Test
    fun `markers less than 150ms apart are linked to the newer one`() {
        val frame = railFrame(events("A" to 0, "B" to 149, "C" to 2_000), 2_000)
        expectThat(frame.markers.map { it.name to (it.linkLength > 0f) }).containsExactly("C" to false, "B" to false, "A" to true)
        expectThat(frame.markers.last().linkLength.toDouble()).isEqualTo(26.0, 0.001)
    }

    @Test
    fun `labels hold for 2s then fade to 0_55 until 9s`() {
        expectThat(railLabelAlpha(2f)).isEqualTo(1f)
        expectThat(railLabelAlpha(9f)).isEqualTo(0f)
        expectThat(railLabelAlpha(8.99f).toDouble()).isEqualTo(0.5506, 0.001)
        expectThat(railLabelAlpha(5.5f).toDouble()).isEqualTo(0.775, 0.001)
    }

    @Test
    fun `markers fade over the top 90dp`() {
        val nearTop = railFrame(events("A" to 0), 16_000).markers.single()
        expectThat(nearTop.markerAlpha.toDouble()).isEqualTo((540.0 - 496.0) / 90.0, 0.001)
    }

    @Test
    fun `events are removed 1_5s after leaving the rail`() {
        val window = (540f / 31f * 1000).toLong() + 1_500
        expectThat(railFrame(events("A" to 0), window).markers.size).isEqualTo(1)
        expectThat(railFrame(events("A" to 0), window + 1).markers).isEmpty()
    }

    @Test
    fun `markers of a burst pop 60ms apart after 40ms`() {
        val frame = railFrame(events("A" to 1_000, "B" to 1_000, "C" to 5_000), 5_000)
        expectThat(frame.markers.map { it.popAt }).containsExactly(5_040L, 1_100L, 1_040L)
    }

    @Test
    fun `a view opens a screen band until the next view`() {
        val frame = railFrame(events("App Started" to 0, "Lines Viewed" to 1_000, "Map Explored" to 2_000, "For You Viewed" to 3_000), 3_000)
        expectThat(frame.bands.map { it.top to it.bottom }).containsExactly(
            (62f + 10f) to (31f - 10f),
            (0f + 10f) to (0f - 10f),
        )
    }

    @Test
    fun `ticks every 5 seconds inside the rail`() {
        expectThat(railFrame(emptyList(), 0).ticks).containsExactly(RailTick(155f, "5s"), RailTick(310f, "10s"), RailTick(465f, "15s"))
    }

    @Test
    fun `demo at 10_6s labels the last 15 events`() {
        val frame = railFrame(OverlayDemo.events(0), 10_600)
        expectThat(frame.markers.count { it.labelVisible }).isEqualTo(15)
        expectThat(frame.markers.size).isEqualTo(17)
    }
}
```

Fix the first band's expected values from the walk. `App Started` at 0 has no band. Lines Viewed (y=62) opens group 1, Map Explored (y=31) extends it, so it spans `top=62+10` to `bottom=31-10`. For You Viewed (y=0) opens group 2 with top=bottom=0. The labels are visible, so check the push: B at 2 s, age 1, y=31 vs For You's 0 + 26 → 31 is fine; Lines at 62 vs 31+26=57 → 62. Keep as written.

- [ ] **Step 2: Run and check they fail.** Expected: compilation FAIL.

- [ ] **Step 3: Implement `RailLayout.kt`.**

```kotlin
package com.sloy.debugmenu.events.overlay

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import kotlin.math.abs

internal object RailSpec {
    const val PX_PER_SECOND = 31f
    const val RAIL_HEIGHT = 540f
    const val LABEL_SECONDS = 9f
    const val HOLD_SECONDS = 2f
    const val LABEL_FADE = 0.45f
    const val LABEL_GAP = 26f
    const val DOT_GAP = 7f
    const val BURST_WINDOW_MILLIS = 150L
    const val EDGE_FADE = 90f
    const val BAND_EXTENT = 10f
    const val REMOVE_AFTER_EXTRA_MILLIS = 1_500L
    const val POP_DELAY_MILLIS = 40L
    const val POP_STAGGER_MILLIS = 60L
    const val TICK_SECONDS = 5
    const val TICK_MARGIN = 10f
    val WINDOW_MILLIS: Long = (RAIL_HEIGHT / PX_PER_SECOND * 1000).toLong() + REMOVE_AFTER_EXTRA_MILLIS
}

internal data class RailMarker(
    val key: String,
    val name: String,
    val type: EventType,
    val y: Float,
    val trueY: Float,
    val labelVisible: Boolean,
    val labelAlpha: Float,
    val markerAlpha: Float,
    val linkLength: Float,
    val popAt: Long,
)

internal data class RailBand(val key: String, val top: Float, val bottom: Float, val alpha: Float)

internal data class RailTick(val y: Float, val label: String)

internal data class RailFrame(val markers: List<RailMarker>, val bands: List<RailBand>, val ticks: List<RailTick>)

internal fun railFrame(events: List<CapturedEvent>, nowMillis: Long): RailFrame {
    val live = events.chronological().filter { it.timestampMillis <= nowMillis && nowMillis - it.timestampMillis <= RailSpec.WINDOW_MILLIS }
    val popAts = popTimes(live)
    val markers = ArrayList<RailMarker>(live.size)
    var newer: RailMarker? = null
    var newerTimestamp = 0L
    for (index in live.indices.reversed()) {
        val event = live[index]
        val ageSeconds = (nowMillis - event.timestampMillis) / 1000f
        val trueY = ageSeconds * RailSpec.PX_PER_SECOND
        val labelVisible = ageSeconds < RailSpec.LABEL_SECONDS
        val y = newer?.let { maxOf(trueY, it.y + if (labelVisible && it.labelVisible) RailSpec.LABEL_GAP else RailSpec.DOT_GAP) } ?: trueY
        val link = newer?.takeIf { abs(newerTimestamp - event.timestampMillis) < RailSpec.BURST_WINDOW_MILLIS }?.let { y - it.y } ?: 0f
        val marker = RailMarker(
            key = event.id,
            name = event.name,
            type = event.type,
            y = y,
            trueY = trueY,
            labelVisible = labelVisible,
            labelAlpha = railLabelAlpha(ageSeconds),
            markerAlpha = edgeAlpha(y),
            linkLength = link,
            popAt = popAts[index],
        )
        markers += marker
        newer = marker
        newerTimestamp = event.timestampMillis
    }
    return RailFrame(markers, screenBands(markers), ticks())
}

internal fun railLabelAlpha(ageSeconds: Float): Float = when {
    ageSeconds >= RailSpec.LABEL_SECONDS -> 0f
    ageSeconds <= RailSpec.HOLD_SECONDS -> 1f
    else -> 1f - RailSpec.LABEL_FADE * (ageSeconds - RailSpec.HOLD_SECONDS) / (RailSpec.LABEL_SECONDS - RailSpec.HOLD_SECONDS)
}

private fun edgeAlpha(y: Float): Float = ((RailSpec.RAIL_HEIGHT - y) / RailSpec.EDGE_FADE).coerceIn(0f, 1f)

private fun popTimes(chronological: List<CapturedEvent>): LongArray {
    val popAts = LongArray(chronological.size)
    chronological.forEachIndexed { index, event ->
        val earliest = event.timestampMillis + RailSpec.POP_DELAY_MILLIS
        popAts[index] = if (index == 0) earliest else maxOf(earliest, popAts[index - 1] + RailSpec.POP_STAGGER_MILLIS)
    }
    return popAts
}

private fun screenBands(newestFirst: List<RailMarker>): List<RailBand> {
    val groups = mutableListOf<Triple<String, Float, Float>>()
    for (marker in newestFirst.asReversed()) {
        if (marker.type == EventType.VIEW) {
            groups += Triple(marker.key, marker.y, marker.y)
        } else if (groups.isNotEmpty()) {
            groups[groups.lastIndex] = groups.last().copy(third = marker.y)
        }
    }
    return groups.map { (key, top, bottom) ->
        RailBand(key, top + RailSpec.BAND_EXTENT, bottom - RailSpec.BAND_EXTENT, edgeAlpha(top))
    }
}

private fun ticks(): List<RailTick> =
    generateSequence(RailSpec.TICK_SECONDS) { it + RailSpec.TICK_SECONDS }
        .map { seconds -> RailTick(seconds * RailSpec.PX_PER_SECOND, "${seconds}s") }
        .takeWhile { it.y < RailSpec.RAIL_HEIGHT - RailSpec.TICK_MARGIN }
        .toList()
```

- [ ] **Step 4: Run the tests.** Expected: PASS. If the `demo` marker count differs, recount by hand: markers are dropped at 18.92 s, so all 17 events up to 10.6 s are live. Labels need age < 9 s → events after 1.6 s → 15.
- [ ] **Step 5: Commit.** `git commit -m "feat(debug): timeline rail layout"`

---

### Task 7: Viewer timeline items and search (pure)

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/EventSearch.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/ViewerItems.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/ViewerSampleData.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/viewer/EventSearchTest.kt`, `TimelineItemsTest.kt`

**Interfaces:**
- Consumes: `chronological()`, `formatDuration`, `formatDelta` (Task 2).
- Produces:
  - `internal fun CapturedEvent.matches(query: String): Boolean`.
  - `internal val CapturedEvent.isSessionSummary: Boolean`.
  - `internal sealed interface ViewerItem { val key: String }` with `EventItem(event, delta, inBand = false)`, `SessionItem(event)`, `GapItem(key, label)`, `BandHeaderItem(key, screen, startMillis, duration: String?)`, `BandEndItem(key)`.
  - `internal fun timelineItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem>`.
  - `internal object ViewerSampleData { val startMillis: Long; val events: List<CapturedEvent> /* newest first */; fun id(index: Int): String }`.

- [ ] **Step 1: Write the sample data** (from `ViewerTimeline.dc.html`, t = seconds since 16:21:00 local time on 2026-10-08):

```kotlin
package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import java.time.LocalDateTime
import java.time.ZoneId

internal object ViewerSampleData {
    val startMillis: Long = LocalDateTime.of(2026, 10, 8, 16, 21, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private val DATA: List<Triple<Long, String, Map<String, String>>> = listOf(
        Triple(1_050, "App Started", emptyMap()),
        Triple(1_120, "For You Viewed", mapOf("trigger" to "launch")),
        Triple(1_910, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "9", "latencyMs" to "388")),
        Triple(2_400, "Lines Viewed", mapOf("trigger" to "navigation")),
        Triple(2_850, "Line Paths Displayed", mapOf("pathCount" to "15")),
        Triple(3_600, "Map Explored", emptyMap()),
        Triple(4_240, "Map Stop Clicked", mapOf("stopId" to "412")),
        Triple(4_255, "Stop Details Viewed", mapOf("stopId" to "412", "source" to "map", "trigger" to "navigation")),
        Triple(4_900, "Arrivals Displayed", mapOf("screen" to "stop_detail", "stopId" to "412", "arrivalsCount" to "6", "latencyMs" to "301")),
        Triple(4_910, "Bottom Sheet Changed", mapOf("state" to "partial")),
        Triple(7_480, "Stop Details Closed", mapOf("stopId" to "412", "durationSeconds" to "3", "refreshes" to "0")),
        Triple(7_500, "For You Viewed", mapOf("trigger" to "navigation")),
        Triple(8_100, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "9", "latencyMs" to "405")),
        Triple(9_320, "Favorite Stop Clicked", mapOf("stopId" to "136")),
        Triple(9_335, "Stop Details Viewed", mapOf("stopId" to "136", "source" to "favorites", "trigger" to "navigation")),
        Triple(9_950, "Arrivals Displayed", mapOf("screen" to "stop_detail", "stopId" to "136", "arrivalsCount" to "4", "latencyMs" to "212")),
        Triple(12_700, "Stop Details Closed", mapOf("stopId" to "136", "durationSeconds" to "3", "refreshes" to "0")),
        Triple(12_720, "For You Viewed", mapOf("trigger" to "back")),
        Triple(
            15_000, "Session Summary", mapOf(
                "sessionType" to "explorer", "durationSeconds" to "14", "stopViews" to "2", "distinctStops" to "2",
                "arrivalsViews" to "4", "entrySources" to "[map, favorites]", "featuresUsed" to "[lines, map, favorites]",
                "lastScreen" to "for_you",
            )
        ),
        Triple(145_300, "Arrivals Displayed", mapOf("screen" to "favorites", "stopCount" to "3", "arrivalsCount" to "8", "latencyMs" to "420")),
    )

    fun id(index: Int): String = "sample-$index"

    val events: List<CapturedEvent> = DATA.mapIndexed { index, (at, name, properties) ->
        CapturedEvent(name, properties, startMillis + at, id(index))
    }.asReversed()

    val expandedEventId: String = id(12)
    val sessionSummaryId: String = id(18)
}
```

- [ ] **Step 2: Write the failing tests.** `EventSearchTest.kt`:

```kotlin
class EventSearchTest {
    private val event = CapturedEvent("Map Stop Clicked", mapOf("stopId" to "412", "source" to "map"), timestampMillis = 0)

    @Test fun `empty query matches everything`() { expectThat(event.matches("  ")).isTrue() }
    @Test fun `matches the name ignoring case`() { expectThat(event.matches("stop clicked")).isTrue() }
    @Test fun `matches a key`() { expectThat(event.matches("STOPID")).isTrue() }
    @Test fun `matches a value`() { expectThat(event.matches("412")).isTrue() }
    @Test fun `matches key equals value`() { expectThat(event.matches("stopId=412")).isTrue() }
    @Test fun `matches key space value`() { expectThat(event.matches("source map")).isTrue() }
    @Test fun `does not match other text`() { expectThat(event.matches("favorites")).isFalse() }
    @Test fun `key and value of different properties do not match together`() { expectThat(event.matches("stopId=map")).isFalse() }
}
```

`TimelineItemsTest.kt`:

```kotlin
package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class TimelineItemsTest {

    private fun describe(items: List<ViewerItem>): List<String> = items.map { item ->
        when (item) {
            is EventItem -> "${item.event.name} ${item.delta}"
            is SessionItem -> "[session]"
            is GapItem -> item.label
            is BandHeaderItem -> "<${item.screen} ${item.duration ?: "now"}>"
            is BandEndItem -> "</>"
        }
    }

    @Test
    fun `sample timeline is newest first with deltas gaps and the session card`() {
        val items = describe(timelineItems(ViewerSampleData.events))
        expectThat(items.take(5)).containsExactly(
            "Arrivals Displayed +2m 13s",
            "· 2m 13s in background ·",
            "[session]",
            "For You Viewed +0.02s",
            "Stop Details Closed +2.75s",
        )
        expectThat(items.last()).isEqualTo("App Started first")
        expectThat(items.size).isEqualTo(21)
    }

    @Test
    fun `quiet gap when no session summary lies between`() {
        val events = listOf(
            CapturedEvent("B", timestampMillis = 12_000, id = "b"),
            CapturedEvent("A", timestampMillis = 0, id = "a"),
        )
        expectThat(describe(timelineItems(events))).containsExactly("B +12.00s", "· 12s quiet ·", "A first")
    }

    @Test
    fun `no gap under 10 seconds`() {
        val events = listOf(CapturedEvent("B", timestampMillis = 9_999, id = "b"), CapturedEvent("A", timestampMillis = 0, id = "a"))
        expectThat(timelineItems(events).filterIsInstance<GapItem>()).isEmpty()
    }

    @Test
    fun `sorts by timestamp`() {
        val outOfOrder = listOf(CapturedEvent("A", timestampMillis = 0, id = "a"), CapturedEvent("B", timestampMillis = 500, id = "b"))
        expectThat(describe(timelineItems(outOfOrder))).containsExactly("B +0.50s", "A first")
    }

    @Test
    fun `search keeps matching rows and recomputes gaps between them`() {
        val items = describe(timelineItems(ViewerSampleData.events, query = "stopId=136"))
        expectThat(items).containsExactly(
            "Stop Details Closed +2.75s",
            "Arrivals Displayed +0.61s",
            "Stop Details Viewed +0.02s",
            "Favorite Stop Clicked +1.22s",
        )
    }

    @Test
    fun `no match gives no items`() {
        expectThat(timelineItems(ViewerSampleData.events, query = "nothing like this")).isEmpty()
    }
}
```

(Import `strikt.assertions.isEqualTo`.) Item count 21 = 20 events (19 rows + 1 session card) + 1 gap.

- [ ] **Step 3: Run and check they fail.** Expected: compilation FAIL.

- [ ] **Step 4: Implement.** `EventSearch.kt`:

```kotlin
package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent

internal fun CapturedEvent.matches(query: String): Boolean {
    val text = query.trim()
    if (text.isEmpty() || name.contains(text, ignoreCase = true)) return true
    return properties.any { (key, value) ->
        key.contains(text, ignoreCase = true) ||
            value.contains(text, ignoreCase = true) ||
            "$key=$value".contains(text, ignoreCase = true) ||
            "$key $value".contains(text, ignoreCase = true)
    }
}
```

`ViewerItems.kt` (timeline part; Journey is added in Task 8):

```kotlin
package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.chronological
import com.sloy.debugmenu.events.formatDelta
import com.sloy.debugmenu.events.formatDuration

internal const val SESSION_SUMMARY = "Session Summary"
private const val GAP_MILLIS = 10_000L

internal val CapturedEvent.isSessionSummary: Boolean get() = name == SESSION_SUMMARY

internal sealed interface ViewerItem {
    val key: String
}

internal data class EventItem(val event: CapturedEvent, val delta: String, val inBand: Boolean = false) : ViewerItem {
    override val key: String get() = event.id
}

internal data class SessionItem(val event: CapturedEvent) : ViewerItem {
    override val key: String get() = event.id
}

internal data class GapItem(override val key: String, val label: String) : ViewerItem

internal data class BandHeaderItem(override val key: String, val screen: String, val startMillis: Long, val duration: String?) : ViewerItem

internal data class BandEndItem(override val key: String) : ViewerItem

internal fun timelineItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem> {
    val all = events.chronological()
    val deltas = deltas(all)
    val visible = all.filter { it.matches(query) }
    val items = mutableListOf<ViewerItem>()
    for (index in visible.indices.reversed()) {
        val event = visible[index]
        if (event.isSessionSummary) {
            items += SessionItem(event)
            continue
        }
        items += EventItem(event, deltas.getValue(event.id))
        val older = visible.subList(0, index).lastOrNull { !it.isSessionSummary } ?: continue
        gapBetween(older, event, all)?.let { items += it }
    }
    return items
}

internal fun deltas(chronological: List<CapturedEvent>): Map<String, String> {
    var previous: CapturedEvent? = null
    return buildMap {
        chronological.filterNot { it.isSessionSummary }.forEach { event ->
            put(event.id, previous?.let { formatDelta(event.timestampMillis - it.timestampMillis) } ?: "first")
            previous = event
        }
    }
}

private fun gapBetween(older: CapturedEvent, newer: CapturedEvent, all: List<CapturedEvent>): GapItem? {
    val gap = newer.timestampMillis - older.timestampMillis
    if (gap < GAP_MILLIS) return null
    val inBackground = all.any { it.isSessionSummary && it.timestampMillis in older.timestampMillis..newer.timestampMillis }
    return GapItem("gap-${newer.id}", "· ${formatDuration(gap)} ${if (inBackground) "in background" else "quiet"} ·")
}
```

Deltas come from the full chronology, gaps from the visible rows (agreed search semantics).

- [ ] **Step 5: Run the tests.** Expected: PASS.
- [ ] **Step 6: Commit.** `git commit -m "feat(debug): event viewer timeline items and search"`

---

### Task 8: Journey items and lanes model (pure)

**Files:**
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/ViewerItems.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/Lanes.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/viewer/JourneyItemsTest.kt`, `LanesTest.kt`

**Interfaces:**
- Consumes: Task 7 items, `deltas`, `matches`; `toClockTime`, `formatDuration` (Task 2).
- Produces:
  - `internal fun journeyItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem>`.
  - `internal enum class Lane { SCREENS, CLICKS, EVENTS }`, `internal data class LaneBar(key: String, start: Float, end: Float, label: String)`, `internal data class LaneMark(key: String, x: Float)`, `internal data class LanesModel(range: String, span: String, screens: List<LaneBar>, clicks: List<LaneMark>, events: List<LaneMark>, axis: List<Pair<Float, String>>)`.
  - `internal fun lanesModel(events: List<CapturedEvent>, fromMillis: Long, toMillis: Long): LanesModel`, `internal fun LanesModel.markAt(lane: Lane, x: Float, tolerance: Float): String?`.
  - x values are fractions 0..1 of the lane width. Keys are event ids (a screen bar's key is its view event id).

- [ ] **Step 1: Write the failing tests.** `JourneyItemsTest.kt` (reuse the `describe` helper by copying it in):

```kotlin
    @Test
    fun `sample journey groups by screen newest first with the view as last row`() {
        expectThat(describe(journeyItems(ViewerSampleData.events))).containsExactly(
            "<For You (resumed) now>",
            "Arrivals Displayed +2m 13s",
            "</>",
            "· 2m 13s in background ·",
            "[session]",
            "<For You 2.3s>", "For You Viewed +0.02s", "</>",
            "<Stop Details 3.4s>", "Stop Details Closed +2.75s", "Arrivals Displayed +0.61s", "Stop Details Viewed +0.02s", "</>",
            "<For You 1.8s>", "Favorite Stop Clicked +1.22s", "Arrivals Displayed +0.60s", "For You Viewed +0.02s", "</>",
            "<Stop Details 3.2s>", "Stop Details Closed +2.57s", "Bottom Sheet Changed +0.01s", "Arrivals Displayed +0.65s", "Stop Details Viewed +0.01s", "</>",
            "<Lines 1.9s>", "Map Stop Clicked +0.64s", "Map Explored +0.75s", "Line Paths Displayed +0.45s", "Lines Viewed +0.49s", "</>",
            "<For You 1.3s>", "Arrivals Displayed +0.79s", "For You Viewed +0.07s", "</>",
            "App Started first",
        )
    }

    @Test
    fun `rows in a band are marked in band`() {
        expectThat(journeyItems(ViewerSampleData.events).filterIsInstance<EventItem>().count { !it.inBand }).isEqualTo(1)
    }

    @Test
    fun `events before any view are loose rows`() {
        val events = listOf(CapturedEvent("Map Explored", timestampMillis = 10, id = "b"), CapturedEvent("App Started", timestampMillis = 0, id = "a"))
        expectThat(describe(journeyItems(events))).containsExactly("Map Explored +0.01s", "App Started first")
    }

    @Test
    fun `resumed band without lastScreen`() {
        val events = listOf(
            CapturedEvent("Map Explored", timestampMillis = 20_000, id = "c"),
            CapturedEvent("Session Summary", timestampMillis = 10_000, id = "b"),
            CapturedEvent("App Started", timestampMillis = 0, id = "a"),
        )
        expectThat(describe(journeyItems(events)).first()).isEqualTo("<Resumed now>")
    }

    @Test
    fun `search hides bands without matching rows`() {
        val items = describe(journeyItems(ViewerSampleData.events, query = "stopId=412"))
        expectThat(items).containsExactly(
            "<Stop Details 3.2s>", "Stop Details Closed +2.57s", "Arrivals Displayed +0.65s", "Stop Details Viewed +0.01s", "</>",
            "<Lines 1.9s>", "Map Stop Clicked +0.64s", "</>",
        )
    }
```

`Arrivals Displayed +0.65s` is 4.900 − 4.255 = 0.645 s rounded half-up by `formatDelta` (Task 2), matching the reference screenshot. Bottom Sheet Changed has no `stopId`, so the query filters it out of the Stop Details band.

`LanesTest.kt`:

```kotlin
class LanesTest {
    private val start = ViewerSampleData.startMillis

    @Test
    fun `range and span come from the visible items`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.range).isEqualTo("16:21:01 → 16:21:15")
        expectThat(model.span).isEqualTo("14s")
    }

    @Test
    fun `window is padded 5 percent on each side`() {
        val events = listOf(CapturedEvent("A", timestampMillis = 10_000, id = "a"), CapturedEvent("B", timestampMillis = 0, id = "b"))
        val model = lanesModel(events, 0, 10_000)
        expectThat(model.events.map { it.key }).containsExactly("b", "a")
        expectThat(model.events[0].x.toDouble()).isEqualTo(0.05 / 1.1, 0.0001)
        expectThat(model.events[1].x.toDouble()).isEqualTo(1.05 / 1.1, 0.0001)
    }

    @Test
    fun `window is at least 5 seconds`() {
        val events = listOf(CapturedEvent("A", timestampMillis = 1_000, id = "a"))
        val model = lanesModel(events, 1_000, 1_000)
        expectThat(model.events.single().x.toDouble()).isEqualTo(0.5, 0.0001)
    }

    @Test
    fun `screen bars run from a view to the next view or session summary`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.screens.map { it.label }).containsExactly("For You", "Lines", "Stop Details", "For You", "Stop Details", "For You")
        expectThat(model.screens.last().end).isEqualTo(model.events.first { it.key == ViewerSampleData.sessionSummaryId }.x)
    }

    @Test
    fun `clicks and other events go to their lanes`() {
        val model = lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)
        expectThat(model.clicks.size).isEqualTo(2)
        expectThat(model.events.size).isEqualTo(11)
    }

    @Test
    fun `axis has five labels`() {
        expectThat(lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000).axis.size).isEqualTo(5)
    }

    @Test
    fun `tap hits the nearest mark within tolerance`() {
        val model = LanesModel("", "", emptyList(), listOf(LaneMark("a", 0.2f), LaneMark("b", 0.3f)), emptyList(), emptyList())
        expectThat(model.markAt(Lane.CLICKS, 0.27f, 0.05f)).isEqualTo("b")
        expectThat(model.markAt(Lane.CLICKS, 0.6f, 0.05f)).isNull()
    }

    @Test
    fun `tap on a screen bar hits its view`() {
        val model = LanesModel("", "", listOf(LaneBar("v", 0.1f, 0.4f, "Lines")), emptyList(), emptyList(), emptyList())
        expectThat(model.markAt(Lane.SCREENS, 0.3f, 0.05f)).isEqualTo("v")
    }
}
```

Events lane in the sample: the non-view, non-click events from 1.05 to 15.0. That's App Started, Arrivals ×4, Line Paths, Map Explored, Bottom Sheet, Stop Details Closed ×2 and Session Summary = 11.

- [ ] **Step 2: Run and check they fail.** Expected: compilation FAIL.

- [ ] **Step 3: Implement Journey** (append to `ViewerItems.kt`):

```kotlin
private sealed interface Block
private class Band(val key: String, val screen: String, val startMillis: Long, val events: MutableList<CapturedEvent>) : Block {
    var endMillis: Long? = null
}
private class SessionBlock(val event: CapturedEvent) : Block
private class LooseBlock(val event: CapturedEvent) : Block

internal fun journeyItems(events: List<CapturedEvent>, query: String = ""): List<ViewerItem> {
    val all = events.chronological()
    val deltas = deltas(all)
    val blocks = buildBlocks(all)
    val items = mutableListOf<ViewerItem>()
    for (index in blocks.indices.reversed()) {
        when (val block = blocks[index]) {
            is SessionBlock -> if (block.event.matches(query)) items += SessionItem(block.event)
            is LooseBlock -> if (block.event.matches(query)) items += EventItem(block.event, deltas.getValue(block.event.id))
            is Band -> {
                val rows = block.events.filter { it.matches(query) }
                if (rows.isEmpty()) continue
                items += BandHeaderItem("band-${block.key}", block.screen, block.startMillis, block.endMillis?.let { formatDuration(it - block.startMillis) })
                rows.asReversed().forEach { items += EventItem(it, deltas.getValue(it.id), inBand = true) }
                items += BandEndItem("band-end-${block.key}")
                backgroundGap(block, blocks.getOrNull(index - 1), all)?.let { items += it }
            }
        }
    }
    return items
}

private fun buildBlocks(chronological: List<CapturedEvent>): List<Block> {
    val blocks = mutableListOf<Block>()
    var current: Band? = null
    for (event in chronological) {
        when {
            event.isSessionSummary -> {
                current?.endMillis = event.timestampMillis
                current = null
                blocks += SessionBlock(event)
            }
            event.type == EventType.VIEW -> {
                current?.endMillis = event.timestampMillis
                current = Band(event.id, event.name.removeSuffix(" Viewed"), event.timestampMillis, mutableListOf(event)).also { blocks += it }
            }
            current != null -> current.events += event
            blocks.lastOrNull() is SessionBlock -> {
                val session = blocks.last() as SessionBlock
                current = Band("resumed-${event.id}", resumedScreen(session.event), event.timestampMillis, mutableListOf(event)).also { blocks += it }
            }
            else -> blocks += LooseBlock(event)
        }
    }
    return blocks
}

private fun resumedScreen(sessionSummary: CapturedEvent): String {
    val lastScreen = sessionSummary.properties["lastScreen"] ?: return "Resumed"
    return lastScreen.split('_').joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } } + " (resumed)"
}

private fun backgroundGap(band: Band, previous: Block?, all: List<CapturedEvent>): GapItem? {
    if (previous !is SessionBlock) return null
    val lastBefore = all.lastOrNull { !it.isSessionSummary && it.timestampMillis < previous.event.timestampMillis } ?: return null
    return GapItem("gap-${band.key}", "· ${formatDuration(band.startMillis - lastBefore.timestampMillis)} in background ·")
}
```

(Import `com.sloy.debugmenu.events.EventType`.) `current != null -> current.events += event` needs a smart cast: use `current?.let { it.events += event } != null` or restructure with a local `val band = current`. Write it as:

```kotlin
            else -> {
                val band = current
                when {
                    band != null -> band.events += event
                    blocks.lastOrNull() is SessionBlock -> { … }
                    else -> blocks += LooseBlock(event)
                }
            }
```

`Lanes.kt`:

```kotlin
package com.sloy.debugmenu.events.viewer

import com.sloy.debugmenu.events.CapturedEvent
import com.sloy.debugmenu.events.EventType
import com.sloy.debugmenu.events.chronological
import com.sloy.debugmenu.events.formatDuration
import com.sloy.debugmenu.events.toClockTime
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

private const val MIN_SPAN_MILLIS = 5_000L
private const val PADDING_FRACTION = 0.05
private const val AXIS_LABELS = 5

internal enum class Lane { SCREENS, CLICKS, EVENTS }

internal data class LaneBar(val key: String, val start: Float, val end: Float, val label: String)

internal data class LaneMark(val key: String, val x: Float)

internal data class LanesModel(
    val range: String,
    val span: String,
    val screens: List<LaneBar>,
    val clicks: List<LaneMark>,
    val events: List<LaneMark>,
    val axis: List<Pair<Float, String>>,
)

internal fun lanesModel(events: List<CapturedEvent>, fromMillis: Long, toMillis: Long): LanesModel {
    val all = events.chronological()
    val span = maxOf(toMillis - fromMillis, MIN_SPAN_MILLIS)
    val spanStart = fromMillis - (span - (toMillis - fromMillis)) / 2
    val padding = (span * PADDING_FRACTION).toLong()
    val windowStart = spanStart - padding
    val windowEnd = spanStart + span + padding
    fun x(millis: Long): Float = ((millis - windowStart).toDouble() / (windowEnd - windowStart)).toFloat()
    fun inWindow(event: CapturedEvent) = event.timestampMillis in windowStart..windowEnd

    val screens = all.withIndex().filter { it.value.type == EventType.VIEW }.mapNotNull { (index, view) ->
        val end = all.drop(index + 1).firstOrNull { it.type == EventType.VIEW || it.isSessionSummary }?.timestampMillis ?: toMillis
        if (end < windowStart || view.timestampMillis > windowEnd) return@mapNotNull null
        LaneBar(view.id, x(view.timestampMillis).coerceIn(0f, 1f), x(end).coerceIn(0f, 1f), view.name.removeSuffix(" Viewed"))
    }
    val clicks = all.filter { it.type == EventType.CLICK && inWindow(it) }.map { LaneMark(it.id, x(it.timestampMillis)) }
    val others = all.filter { it.type == EventType.OTHER && inWindow(it) }.map { LaneMark(it.id, x(it.timestampMillis)) }
    val axis = (0 until AXIS_LABELS).map { step ->
        val fraction = step / (AXIS_LABELS - 1f)
        val millis = windowStart + ((windowEnd - windowStart) * fraction).toLong()
        fraction to String.format(Locale.US, "%02ds", Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).second)
    }
    return LanesModel(
        range = "${fromMillis.toClockTime()} → ${toMillis.toClockTime()}",
        span = formatDuration(toMillis - fromMillis),
        screens = screens,
        clicks = clicks,
        events = others,
        axis = axis,
    )
}

internal fun LanesModel.markAt(lane: Lane, x: Float, tolerance: Float): String? = when (lane) {
    Lane.SCREENS -> screens.firstOrNull { x in it.start..it.end }?.key
    Lane.CLICKS -> clicks.nearest(x, tolerance)
    Lane.EVENTS -> events.nearest(x, tolerance)
}

private fun List<LaneMark>.nearest(x: Float, tolerance: Float): String? =
    filter { abs(it.x - x) <= tolerance }.minByOrNull { abs(it.x - x) }?.key
```

- [ ] **Step 4: Run the tests.** Expected: PASS. If `screen bars …` fails on the last bar's end: the last For You (12.72) ends at the Session Summary (15.0), which is also in the events lane.
- [ ] **Step 5: Commit.** `git commit -m "feat(debug): journey grouping and lanes model"`

---

### Task 9: Stack overlay UI

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/EventsStackOverlay.kt`
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/OverlayPreviewBackground.kt`

**Interfaces:**
- Consumes: `stackFrame`, `StackChip`, `StackSpec` (Task 5); `OverlayColors`, `OverlayEasing`, `EventText` (Task 4); `OverlayDemo` (Task 5).
- Produces:
  - `@Composable internal fun EventsStackOverlay(events: List<CapturedEvent>, nowMillis: Long, modifier: Modifier = Modifier, lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS)`. It fills its parent, and its bottom is the anchor.
  - `@Composable internal fun OverlayPreviewBackground(content: @Composable BoxScope.() -> Unit)`, a neutral 390×844 dp box with an anchor 124 dp above the bottom, used by every overlay preview.

- [ ] **Step 1: Preview background.**

```kotlin
package com.sloy.debugmenu.events.overlay

@Composable
internal fun OverlayPreviewBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .size(390.dp, 844.dp)
            .background(Color(0xFFE9EAEE))
            .padding(bottom = 124.dp),
        content = content,
    )
}
```

(The neutral grey stands in for the map. No SeviBus UI.)

- [ ] **Step 2: Write `EventsStackOverlay.kt`.**

```kotlin
package com.sloy.debugmenu.events.overlay

@Composable
internal fun EventsStackOverlay(
    events: List<CapturedEvent>,
    nowMillis: Long,
    modifier: Modifier = Modifier,
    lifetimeMillis: Long = StackSpec.LIFETIME_MILLIS,
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val frame = stackFrame(events, nowMillis, maxHeight.value - StackSpec.TOP_RESERVE, lifetimeMillis)
        frame.chips.asReversed().forEach { chip ->
            key(chip.key) {
                StackChipItem(chip, Modifier.align(Alignment.BottomEnd).padding(end = 12.dp))
            }
        }
        OlderPill(frame.olderCount, frame.olderY, Modifier.align(Alignment.BottomEnd).padding(end = 12.dp))
    }
}

@Composable
private fun StackChipItem(chip: StackChip, modifier: Modifier) {
    val isStatic = LocalInspectionMode.current
    val offsetY = remember { Animatable(if (isStatic) chip.y else chip.y + 12f) }
    val scale = remember { Animatable(if (isStatic) chip.scale else 0.5f) }
    val alpha = remember { Animatable(if (isStatic) chip.alpha else 0f) }
    val exitX = remember { Animatable(0f) }
    var previousCount by remember { mutableIntStateOf(chip.count) }

    LaunchedEffect(chip.y) { offsetY.animateTo(chip.y, tween(440, easing = OverlayEasing.SpringyOut)) }
    LaunchedEffect(chip.scale) { scale.animateTo(chip.scale, tween(440, easing = OverlayEasing.SpringyOut)) }
    LaunchedEffect(chip.alpha, chip.phase) {
        val spec = if (chip.phase == ChipPhase.OUT) tween<Float>(240, easing = OverlayEasing.CssEaseIn) else tween(250, easing = LinearEasing)
        alpha.animateTo(chip.alpha, spec)
    }
    LaunchedEffect(chip.phase) {
        if (chip.phase == ChipPhase.OUT) exitX.animateTo(40f, tween(320, easing = OverlayEasing.ExitAccel))
    }
    LaunchedEffect(chip.count) {
        if (chip.count > previousCount) {
            launch { scale.animateTo(1.08f, tween(440, easing = OverlayEasing.SpringyOut)) }
            delay(150)
            scale.animateTo(chip.scale, tween(440, easing = OverlayEasing.SpringyOut))
        }
        previousCount = chip.count
    }

    StackChipContent(
        chip = chip,
        modifier = modifier.graphicsLayer {
            translationY = offsetY.value.dp.toPx()
            translationX = exitX.value.dp.toPx()
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
            transformOrigin = TransformOrigin(1f, 0.5f)
        },
    )
}

@Composable
private fun StackChipContent(chip: StackChip, modifier: Modifier = Modifier) {
    val accent = chip.type.onDark
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(28.dp)
            .widthIn(max = 330.dp)
            .clip(CircleShape)
            .background(OverlayColors.Glass)
            .drawBehind {
                val inset = 12.dp.toPx()
                val lineHeight = 2.dp.toPx()
                val width = size.width - inset * 2
                val top = size.height - lineHeight
                drawRect(OverlayColors.TimerTrack, Offset(inset, top), Size(width, lineHeight))
                drawRect(accent, Offset(inset, top), Size(width * chip.timerFraction, lineHeight))
            }
            .padding(start = 10.dp, end = 11.dp),
    ) {
        Box(Modifier.size(8.dp).background(accent, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            chip.name,
            style = EventText.Mono12Medium,
            color = OverlayColors.OnGlass,
            maxLines = 1,
            overflow = TextOverflow.StartEllipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (chip.count > 1) {
            Spacer(Modifier.width(8.dp))
            Text(
                "×${chip.count}",
                style = EventText.Mono11Medium,
                color = OverlayColors.OnGlass,
                modifier = Modifier
                    .background(OverlayColors.Badge, RoundedCornerShape(6.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun OlderPill(count: Int, y: Float, modifier: Modifier) {
    val offsetY by animateFloatAsState(y, tween(440, easing = OverlayEasing.SpringyOut), label = "olderY")
    val alpha by animateFloatAsState(if (count > 0) 1f else 0f, tween(200, easing = OverlayEasing.CssEase), label = "olderAlpha")
    if (count == 0 && alpha == 0f) return
    Text(
        "+$count older",
        style = EventText.Mono10Medium,
        color = OverlayColors.OnGlass,
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.dp.toPx()
                this.alpha = alpha
            }
            .height(20.dp)
            .background(OverlayColors.GlassMuted, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp)
            .wrapContentHeight(Alignment.CenterVertically),
    )
}
```

Burst stagger A9 comes from `stackFrame` (`enterAt`). Each chip only appears in the frame when its turn comes, and its enter animation starts then.

- [ ] **Step 3: Add previews.** At the end of the file:

```kotlin
@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayDemoPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 10_600) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayLaterPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 12_300) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsStackOverlayOverflowPreview() {
    OverlayPreviewBackground { EventsStackOverlay(OverlayDemo.events(0), nowMillis = 12_600, lifetimeMillis = 20_000) }
}
```

(The overlay is identical in light and dark; `@PreviewLightDark` is kept for consistency with the request. A light-only `@Preview` is acceptable if you'd rather avoid duplicate references.)

- [ ] **Step 4: Render and compare.** Run `./gradlew :debug-menu:updateDebugScreenshotTest --tests '*EventsStackOverlay*'`. Open each new PNG under `debug-menu/src/screenshotTestDebug/reference/com/sloy/debugmenu/ScreensScreenshotTests/` next to `$HANDOFF/screenshots/overlay-stack-t10.6s.png`, `overlay-stack-t12.3s.png` and `overlay-stack-overflow-t12.6s.png`. Check:
  - the chip order and the ×N badges,
  - the 3 and 9 dp gaps,
  - the end inset,
  - the faded oldest chip,
  - the timer line lengths,
  - the "+1 older" pill and the folded chip peeking out.

  Expected differences: no map, no blur, no app chrome. Write down any other difference.

- [ ] **Step 5: Commit.** `git add debug-menu && git commit -m "feat(debug): spring stack events overlay"`

---

### Task 9b: Rail overlay UI

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/EventsRailOverlay.kt`

**Interfaces:**
- Consumes: `railFrame`, `RailMarker`, `RailBand`, `RailTick`, `RailSpec` (Task 6), styles (Task 4), `OverlayPreviewBackground` (Task 9).
- Produces: `@Composable internal fun EventsRailOverlay(events: List<CapturedEvent>, nowMillis: Long, modifier: Modifier = Modifier)`. It fills its parent, and its bottom is the anchor: the strip bottom is 10 dp below it and the marker origin (y = 0) 4 dp below it.

- [ ] **Step 1: Write the rail.** Layout: a `Box(Modifier.fillMaxSize().offset(y = 10.dp))`. Every element aligns `BottomEnd` and is placed with `graphicsLayer { translationY = -(ORIGIN + y − h/2).dp.toPx() }`, where `ORIGIN = 6f` dp from the strip bottom and `h` is the element's height.

```kotlin
private const val ORIGIN = 6f
private const val STRIP_END = 12f
private const val STRIP_WIDTH = 18f
private const val CENTRE_END = STRIP_END + STRIP_WIDTH / 2
private const val LABEL_END = CENTRE_END + 14f
private const val TICK_END = STRIP_END + STRIP_WIDTH + 2f

private fun Modifier.atRailY(y: Float, height: Float) = graphicsLayer { translationY = -(ORIGIN + y - height / 2).dp.toPx() }

@Composable
internal fun EventsRailOverlay(events: List<CapturedEvent>, nowMillis: Long, modifier: Modifier = Modifier) {
    val frame = railFrame(events, nowMillis)
    Box(modifier.fillMaxSize().offset(y = 10.dp)) {
        RailStrip(Modifier.align(Alignment.BottomEnd).padding(end = STRIP_END.dp))
        frame.ticks.forEach { tick -> RailTickLabel(tick, Modifier.align(Alignment.BottomEnd).padding(end = TICK_END.dp)) }
        frame.bands.forEach { band ->
            key(band.key) {
                val height = band.top - band.bottom
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = STRIP_END.dp)
                        .atRailY((band.top + band.bottom) / 2, height)
                        .graphicsLayer { alpha = band.alpha }
                        .size(STRIP_WIDTH.dp, height.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(OverlayColors.RailBandFill)
                        .border(1.dp, OverlayColors.RailBandBorder, RoundedCornerShape(9.dp))
                )
            }
        }
        frame.markers.asReversed().forEach { marker ->
            key(marker.key) { RailEvent(marker, nowMillis) }
        }
        NowHead(Modifier.align(Alignment.BottomEnd).padding(end = (CENTRE_END - 4f).dp))
    }
}
```

`RailStrip`: a `Box(size(18.dp, (RailSpec.RAIL_HEIGHT + 12).dp), clip r9, background RailTrack, border 1.dp RailTrackBorder)` containing a centred `Box(width 1.dp, fillMaxHeight, padding vertical 6.dp, background RailLine)`.

`RailTickLabel`: a `Row(verticalAlignment = CenterVertically, modifier.atRailY(tick.y, 12f))` with `Text(tick.label, EventText.Mono9Medium.copy(shadow = Shadow(Color.White, blurRadius = 6f)), color = OverlayColors.RailTickText)`, `Spacer(3.dp)` and `Box(size(5.dp, 1.dp).background(OverlayColors.RailTick))`.

`RailEvent(marker, nowMillis)` is a `BoxScope` extension placing three things:
- **Link** (when `marker.linkLength > 0`): `Box(align BottomEnd, padding end (CENTRE_END − 1.5).dp, atRailY(marker.y − marker.linkLength / 2, marker.linkLength), size(3.dp, marker.linkLength.dp), background RailLink, graphicsLayer alpha = marker.markerAlpha)`.
- **Marker** (scale from pop, y with the animated push):

  ```kotlin
  val isStatic = LocalInspectionMode.current
  val pop = remember { Animatable(if (isStatic) 1f else 0f) }
  LaunchedEffect(Unit) {
      delay((marker.popAt - System.currentTimeMillis()).coerceAtLeast(0))
      pop.animateTo(1f, tween(420, easing = OverlayEasing.PopOut))
  }
  val push by animateFloatAsState(marker.y - marker.trueY, tween(250, easing = LinearEasing), label = "push")
  val y = marker.trueY + push
  ```

  The shape is a `Box(align BottomEnd, padding end (CENTRE_END − w/2).dp, atRailY(y, h), graphicsLayer { scaleX = pop; scaleY = pop; alpha = marker.markerAlpha; rotationZ = if (CLICK) 45f else 0f })`:
  - VIEW: `size(10.dp, 14.dp)`, shape `RoundedCornerShape(3.dp)`, `shadow(1.dp, shape, ambientColor = MarkerShadow)`, `background(White)`, `border(2.dp, EventType.VIEW.color, shape)`.
  - CLICK: `size(10.dp)`, shape `RoundedCornerShape(2.dp)`, shadow, `border(1.5.dp, White, shape)`, `background(EventType.CLICK.color, shape)`.
  - OTHER: `size(11.dp)`, `CircleShape`, shadow, `background(EventType.OTHER.color, CircleShape)`, `border(1.5.dp, White, CircleShape)`.

  Order the modifiers so the ring is outside the fill: shadow → background → border.
- **Label:**

  ```kotlin
  val shown = remember { Animatable(if (isStatic && marker.labelVisible) 1f else 0f) }
  LaunchedEffect(marker.labelVisible) { shown.animateTo(if (marker.labelVisible) 1f else 0f, tween(360, easing = OverlayEasing.LabelOut)) }
  val fade = remember { Animatable(if (isStatic && marker.labelVisible) 1f else 0f) }
  LaunchedEffect(marker.labelVisible) { fade.animateTo(if (marker.labelVisible) 1f else 0f, tween(250, easing = LinearEasing)) }
  if (marker.labelVisible || fade.value > 0f) RailLabel(marker, Modifier.align(Alignment.BottomEnd).padding(end = LABEL_END.dp).atRailY(y, 24f).graphicsLayer {
      alpha = fade.value * marker.labelAlpha.coerceAtLeast(if (marker.labelVisible) 0f else 0.55f) * marker.markerAlpha
      translationX = (1f - shown.value) * 10.dp.toPx()
      val scale = 0.6f + 0.4f * shown.value
      scaleX = scale; scaleY = scale
      transformOrigin = TransformOrigin(1f, 0.5f)
  })
  ```

  `RailLabel` is a `Row(height 24.dp, clip r12, background Glass, padding(start 8.dp, end 9.dp), CenterVertically)` with `Box(7.dp circle, type.onDark)`, `Spacer(6.dp)` and `Text(name, Mono12Medium, OnGlass, maxLines 1, StartEllipsis, widthIn(max = 300.dp))`.

`NowHead`: an infinite transition `rememberInfiniteTransition()`, with `animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = OverlayEasing.CssEaseOut), RepeatMode.Restart))` = progress. Draw a ring `Box(8.dp circle, primary @ alpha 0.55*(1-progress), scale 1+1.6*progress)` and a dot `Box(6.dp, primary, CircleShape)`, both centred at `atRailY(0f, 8f)`. In inspection mode, use `progress = 0f`.

Bands and links follow the raw frame values, without the 250 ms push animation. The difference only shows during a push (note it in the verification list).

- [ ] **Step 2: Previews.**

```kotlin
@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsRailOverlayDemoPreview() {
    OverlayPreviewBackground { EventsRailOverlay(OverlayDemo.events(0), nowMillis = 10_600) }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventsRailOverlayLaterPreview() {
    OverlayPreviewBackground { EventsRailOverlay(OverlayDemo.events(0), nowMillis = 12_300) }
}
```

- [ ] **Step 3: Render and compare** with `$HANDOFF/screenshots/overlay-rail-push-t10.6s.png`, `overlay-rail-push-t12.3s.png` and the `components/rail-*.png`. Check:
  - 15 labels and 17 markers at 10.6 s,
  - push spacing,
  - bands from each view,
  - burst links,
  - the ticks at 5/10/15 s,
  - the head at the bottom.

  The head colour is the stock M3 primary in the preview (intended).
- [ ] **Step 4: Commit.** `git commit -m "feat(debug): timeline rail events overlay"`

---

### Task 10: Overlay plumbing, Events module and host slot

**Files:**
- Create: `debug-menu/src/main/java/com/sloy/debugmenu/events/overlay/EventsOverlay.kt`
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventsDebugModuleState.kt`, `EventsDebugModuleViewModel.kt`, `EventsModule.kt`
- Delete: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventOverlayLoggerItem.kt`
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/overlay/DebugMenuHost.kt`, `debug-menu-noop/src/main/java/com/sloy/debugmenu/overlay/DebugMenuHost.kt`
- Create: `app/src/debug/java/com/sloy/sevibus/feature/debug/SevDebugOverlay.kt`, `app/src/release/java/com/sloy/sevibus/feature/debug/SevDebugOverlay.kt`
- Modify: `app/src/debug/java/com/sloy/sevibus/feature/debug/SevDebugMenu.kt`, `app/src/main/java/com/sloy/sevibus/App.kt`
- Test: `debug-menu/src/test/java/com/sloy/debugmenu/events/EventsDebugModuleViewModelTest.kt` (new)

**Interfaces:**
- Consumes: `EventsStackOverlay` (Task 9), `EventsRailOverlay` (Task 9b).
- Produces:
  - `EventsDebugModuleState(isOverlayEnabled = false, useTimelineRail = false)`.
  - `EventsDebugModuleViewModel(dataSource, eventStore)` with `onOverlayToggled(Boolean)`, `onTimelineRailToggled(Boolean)`, `onClearEvents()`.
  - `DebugMenuScope.EventsModule(dataSource, eventStore)`.
  - `@Composable fun EventsOverlay(eventStore: EventStore, dataSource: EventsDebugModuleDataSource, modifier: Modifier = Modifier)`.
  - `DebugMenuHost(overlayLogger, menu, modifier = Modifier, overlayBottomPadding: Dp = 0.dp, overlay: @Composable () -> Unit = {}, content)`.

- [ ] **Step 1: Write the failing ViewModel test.**

```kotlin
package com.sloy.debugmenu.events

import com.sloy.debugmenu.testing.testContext
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class EventsDebugModuleViewModelTest {

    private val dataSource = EventsDebugModuleDataSource(testContext())
    private val eventStore = EventStore()
    private val viewModel = EventsDebugModuleViewModel(dataSource, eventStore)

    @Test
    fun `timeline rail is off by default`() {
        expectThat(dataSource.getCurrentState().useTimelineRail).isFalse()
    }

    @Test
    fun `toggling the timeline rail persists it`() {
        viewModel.onTimelineRailToggled(true)
        expectThat(dataSource.getCurrentState().useTimelineRail).isTrue()
    }

    @Test
    fun `toggling the overlay keeps the rail choice`() {
        viewModel.onTimelineRailToggled(true)
        viewModel.onOverlayToggled(true)
        viewModel.onOverlayToggled(false)
        expectThat(dataSource.getCurrentState().isOverlayEnabled).isFalse()
        expectThat(dataSource.getCurrentState().useTimelineRail).isTrue()
    }

    @Test
    fun `clear empties the store`() {
        eventStore.add(CapturedEvent("A", timestampMillis = 0))
        viewModel.onClearEvents()
        expectThat(eventStore.events.value).isEmpty()
    }
}
```

- [ ] **Step 2: Run and check it fails.** Expected: compilation FAIL.

- [ ] **Step 3: Implement the module.**
  - State: `val useTimelineRail: Boolean = false`.
  - ViewModel: drop `overlayLogger`. `onOverlayToggled` only updates the state, because the overlay leaves composition when disabled, which removes every chip at once. Add `fun onTimelineRailToggled(enabled: Boolean) = dataSource.updateState(dataSource.getCurrentState().copy(useTimelineRail = enabled))`.
  - `EventsModule(dataSource, eventStore)`: pass `onTimelineRailToggled`. In `EventsModuleContent`, right after the overlay `DebugCell`:

    ```kotlin
    if (state.isOverlayEnabled) {
        DebugCell(
            title = "Use timeline rail",
            subtitle = "Show events on a vertical timeline instead of a stack",
            onClick = { onTimelineRailToggled(!state.useTimelineRail) },
            start = { Checkbox(checked = state.useTimelineRail, onCheckedChange = onTimelineRailToggled) },
        )
    }
    ```

  - Make the preview `internal fun EventsModuleOverlayEnabledPreview()` with `@ScreenshotTest(ScreenshotSuite.Components)`.
  - `onViewAll` still opens `EventLogScreen(eventStore, onClose)`.
  - Delete `EventOverlayLoggerItem.kt`.

- [ ] **Step 4: Implement `EventsOverlay.kt`.**

```kotlin
package com.sloy.debugmenu.events.overlay

private const val CLOCK_IDLE_AFTER_MILLIS = 25_000L

/**
 * Live events overlay: the spring stack, or the timeline rail when enabled in the Events module.
 * It ignores touches and accessibility, and only shows events tracked after it was turned on.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EventsOverlay(eventStore: EventStore, dataSource: EventsDebugModuleDataSource, modifier: Modifier = Modifier) {
    val state by dataSource.observeCurrentState().collectAsStateWithLifecycle()
    if (state.isOverlayEnabled) {
        val events by eventStore.events.collectAsStateWithLifecycle()
        val startedAt = rememberSaveable(state.useTimelineRail) { System.currentTimeMillis() }
        val recent = remember(events, startedAt) { events.filter { it.timestampMillis >= startedAt } }
        val now = rememberOverlayClock(recent)
        Box(
            modifier
                .fillMaxSize()
                .clearAndSetSemantics {}
                .pointerInteropFilter { false },
        ) {
            if (state.useTimelineRail) EventsRailOverlay(recent, now) else EventsStackOverlay(recent, now)
        }
    }
}

@Composable
private fun rememberOverlayClock(events: List<CapturedEvent>): Long {
    val newest = events.maxOfOrNull { it.timestampMillis }
    return produceState(System.currentTimeMillis(), newest) {
        while (newest != null && System.currentTimeMillis() - newest <= CLOCK_IDLE_AFTER_MILLIS) {
            withFrameMillis { }
            value = System.currentTimeMillis()
        }
        value = System.currentTimeMillis()
    }.value
}
```

25 s covers the rail window (18.9 s) and the stack lifetime. After that the clock stops and nothing recomposes.

- [ ] **Step 5: Host slot and noop.**

```kotlin
@Composable
fun DebugMenuHost(
    overlayLogger: OverlayLogger,
    menu: @Composable DebugMenuScope.() -> Unit,
    modifier: Modifier = Modifier,
    overlayBottomPadding: Dp = 0.dp,
    overlay: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    // …
    Box(modifier.fillMaxSize()) {
        content()
        Box(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = overlayBottomPadding)
        ) { overlay() }
        OverlayLoggerLayer(overlayLogger)
        FloatingDebugButton(…)
    }
```

Update the KDoc: "…with the debug overlays (the [overlay] slot, anchored [overlayBottomPadding] above the safe drawing area's bottom, and the [overlayLogger] items), the floating debug button and the debug [menu] sheet." In `debug-menu-noop`, add the same two parameters (`overlayBottomPadding: Dp = 0.dp`, `overlay: @Composable () -> Unit = {}`) and keep calling only `content()`. Add the `androidx.compose.ui.unit` imports, and check the noop module's dependencies include `ui-unit` (it has compose ui).

- [ ] **Step 6: Wire the app.**

`app/src/debug/.../feature/debug/SevDebugOverlay.kt`:

```kotlin
package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable
import com.sloy.debugmenu.events.overlay.EventsOverlay
import org.koin.compose.koinInject

@Composable
fun SevDebugOverlay() {
    EventsOverlay(eventStore = koinInject(), dataSource = koinInject())
}
```

`app/src/release/.../feature/debug/SevDebugOverlay.kt`:

```kotlin
package com.sloy.sevibus.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun SevDebugOverlay() = Unit
```

`SevDebugMenu.kt`: `EventsModule(koinInject(), koinInject())`.

`App.kt`: `DebugMenuHost(overlayLogger, menu = { SevDebugMenu() }, overlayBottomPadding = DebugOverlayBottomPadding, overlay = { SevDebugOverlay() }) {`. At the end of the file, add `private val DebugOverlayBottomPadding = 100.dp`. That's the 80 dp navigation bar plus 20 dp clearance inside the safe drawing area; Task 14 tunes it on a device. Import `com.sloy.sevibus.feature.debug.SevDebugOverlay` and `androidx.compose.ui.unit.dp`.

- [ ] **Step 7: Run all the tests and both builds.**

Run: `./gradlew :debug-menu:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease`
Expected: PASS and BUILD SUCCESSFUL. If `assembleRelease` needs signing secrets locally, use `:app:compileReleaseKotlin` instead. `grep -rn "EventOverlayLoggerItem" .` must return nothing outside `build/`.

- [ ] **Step 8: Commit.** `git commit -m "feat(debug): events overlay with spring stack and timeline rail"`

---

### Task 11: Viewer components

**Files:**
- Create in `debug-menu/src/main/java/com/sloy/debugmenu/events/viewer/`: `PropertyBox.kt`, `EventRow.kt`, `GapMarker.kt`, `SessionSummaryCard.kt`, `ScreenBand.kt`, `LanesCard.kt`, `ViewerChrome.kt`

**Interfaces:**
- Consumes: Tasks 2, 4, 7, 8.
- Produces (all `internal`, all with `modifier: Modifier = Modifier`):
  - `PropertyBox(properties: Map<String, String>, timestampMillis: Long?)`.
  - `EventRow(event: CapturedEvent, delta: String, expanded: Boolean, onToggle: () -> Unit, inBand: Boolean = false)`.
  - `GapMarker(label: String, inBand: Boolean = false)`.
  - `SessionSummaryCard(event: CapturedEvent, expanded: Boolean, onToggle: () -> Unit)`.
  - `ScreenBandHeader(item: BandHeaderItem)`, `ScreenBandEnd()`, `Modifier.screenBandBackground()`.
  - `LanesCard(model: LanesModel, onMarkClick: (String) -> Unit)`.
  - `EventSearchField(query: String, onQueryChange: (String) -> Unit)`, `CopyJsonBar(onCopy: () -> Unit)`.
  - `const val ANIMATION_MILLIS = 200` (private to the file that uses it).

Each component has its preview with `@ScreenshotTest(ScreenshotSuite.Components) @PreviewLightDark internal fun …Preview()` in `DebugPreviewTheme { Surface(color = MaterialTheme.colorScheme.surface) { … } }`, using `ViewerSampleData`.

- [ ] **Step 1: `PropertyBox.kt`.**

```kotlin
@Composable
internal fun PropertyBox(properties: Map<String, String>, timestampMillis: Long?, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val keys = properties.keys + listOfNotNull("timestamp".takeIf { timestampMillis != null })
    val keyWidth = with(LocalDensity.current) { keys.maxOfOrNull { measurer.measure(it, EventText.Mono12).size.width }?.toDp() ?: 0.dp }
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        properties.forEach { (key, value) -> PropertyLine(key, value, keyWidth, MaterialTheme.colorScheme.onSurface, EventText.Mono12Medium) }
        if (timestampMillis != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            PropertyLine("timestamp", timestampMillis.toClockTimeMillis(), keyWidth, MaterialTheme.colorScheme.onSurfaceVariant, EventText.Mono12)
        }
    }
}

@Composable
private fun PropertyLine(key: String, value: String, keyWidth: Dp, valueColor: Color, valueStyle: TextStyle) {
    Row {
        Text(key, style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(keyWidth))
        Spacer(Modifier.width(16.dp))
        Text(value, style = valueStyle, color = valueColor, modifier = Modifier.weight(1f))
    }
}
```

- [ ] **Step 2: `EventRow.kt`.**

```kotlin
private const val INLINE_PROPERTIES = 3
private const val ANIMATION_MILLIS = 200

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EventRow(
    event: CapturedEvent,
    delta: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    inBand: Boolean = false,
) {
    val hasProperties = event.properties.isNotEmpty()
    val isExpanded = expanded && hasProperties
    val rowBackground = if (isExpanded) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f) else Color.Transparent
    val chevron by animateFloatAsState(if (isExpanded) 180f else 0f, tween(ANIMATION_MILLIS), label = "rowChevron")
    Row(
        modifier
            .fillMaxWidth()
            .background(rowBackground)
            .then(if (hasProperties) Modifier.clickable(onClick = onToggle) else Modifier)
            .height(IntrinsicSize.Min)
            .padding(end = if (inBand) 8.dp else 16.dp),
    ) {
        Column(
            Modifier
                .width(if (inBand) 68.dp else 76.dp)
                .padding(start = if (inBand) 8.dp else 16.dp, top = 11.dp)
        ) {
            Text(event.timestamp, style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurface)
            Text(delta, style = EventText.Mono10, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TimelineMarker(event.type)
        Column(Modifier.weight(1f).padding(top = 11.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                if (hasProperties) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp).rotate(chevron),
                    )
                }
            }
            AnimatedVisibility(
                visible = hasProperties && !isExpanded,
                enter = expandVertically(tween(ANIMATION_MILLIS)) + fadeIn(tween(ANIMATION_MILLIS)),
                exit = shrinkVertically(tween(ANIMATION_MILLIS)) + fadeOut(tween(ANIMATION_MILLIS)),
            ) {
                InlineProperties(event.properties, Modifier.padding(top = 4.dp))
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(ANIMATION_MILLIS)) + fadeIn(tween(ANIMATION_MILLIS)),
                exit = shrinkVertically(tween(ANIMATION_MILLIS)) + fadeOut(tween(ANIMATION_MILLIS)),
            ) {
                PropertyBox(event.properties, event.timestampMillis, Modifier.padding(top = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InlineProperties(properties: Map<String, String>, modifier: Modifier = Modifier) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        properties.entries.take(INLINE_PROPERTIES).forEach { (key, value) ->
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) { append(key) }
                    append(" ")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append(value) }
                },
                style = EventText.Mono12,
            )
        }
        if (properties.size > INLINE_PROPERTIES) {
            Text("+${properties.size - INLINE_PROPERTIES} more", style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun TimelineMarker(type: EventType, modifier: Modifier = Modifier) {
    val colors = type.colors()
    val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Box(modifier.width(24.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(lineColor))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 11.dp)
                .size(20.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .background(colors.tint, CircleShape),
        ) {
            if (type == EventType.VIEW) {
                Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = colors.accent, modifier = Modifier.size(12.dp))
            } else {
                Box(Modifier.size(8.dp).background(colors.accent, CircleShape))
            }
        }
    }
}
```

The `surface` background under the tint is the 3 dp "ring" in the row background, so the line appears to pass behind the marker. Previews:
- `EventRowCollapsedPreview`: the newest sample event, `+2m 13s`, collapsed.
- `EventRowExpandedPreview`: event `ViewerSampleData.expandedEventId`, `+0.60s`, expanded.
- `EventRowNoPropertiesPreview`: Map Explored, `+0.75s`.

- [ ] **Step 3: `GapMarker.kt`.**

```kotlin
@Composable
internal fun GapMarker(label: String, modifier: Modifier = Modifier, inBand: Boolean = false) {
    val dash = MaterialTheme.colorScheme.outline
    Row(modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.width(if (inBand) 68.dp else 76.dp))
        Canvas(Modifier.width(24.dp).fillMaxHeight()) {
            drawLine(dash, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
        }
        Text(label, style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

Preview: `GapMarkerBackgroundPreview` with `"· 2m 13s in background ·"`.

- [ ] **Step 4: `SessionSummaryCard.kt`.**

```kotlin
@Composable
internal fun SessionSummaryCard(event: CapturedEvent, expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = EventType.OTHER.colors()
    val shape = RoundedCornerShape(24.dp)
    val chevron by animateFloatAsState(if (expanded) 180f else 0f, tween(200), label = "sessionChevron")
    Column(
        modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .background(colors.accent.copy(alpha = 0.05f))
    ) {
        Row(Modifier.clickable(onClick = onToggle).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconSpot(Icons.Outlined.DarkMode)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Session Summary", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    event.properties["sessionType"]?.let { type ->
                        Text(
                            type.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.ink,
                            modifier = Modifier.background(colors.tint, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                    event.properties["durationSeconds"]?.toLongOrNull()?.let {
                        Text(formatSeconds(it), style = EventText.Mono12Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("· ${event.timestamp}", style = EventText.Mono12, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = if (expanded) "Collapse" else "Expand", modifier = Modifier.size(24.dp).rotate(chevron))
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(tween(200)) + fadeIn(tween(200)), exit = shrinkVertically(tween(200)) + fadeOut(tween(200))) {
            PropertyBox(event.properties, timestampMillis = null, modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp))
        }
    }
}
```

Previews:
- `SessionSummaryCardCollapsedPreview` and `SessionSummaryCardExpandedPreview`, both with the sample summary.
- `SessionSummaryCardNoDurationPreview`: `CapturedEvent("Session Summary", mapOf("sessionType" to "glancer"), …)`. It must render without a duration (Review Focus 4).

- [ ] **Step 5: `ScreenBand.kt`.**

```kotlin
private val BandShapeTop = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
private val BandShapeBottom = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)

@Composable
internal fun Modifier.screenBandBackground(): Modifier = padding(horizontal = 8.dp).background(screenBandColor(isDarkSurface()))

@Composable
internal fun ScreenBandHeader(item: BandHeaderItem, modifier: Modifier = Modifier) {
    val view = EventType.VIEW.colors()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(start = 8.dp, end = 8.dp, top = 8.dp)
            .fillMaxWidth()
            .background(screenBandColor(isDarkSurface()), BandShapeTop)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Text(item.startMillis.toClockTime(), style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(60.dp))
        Box(Modifier.size(24.dp).background(view.tint, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PhoneAndroid, contentDescription = null, tint = view.accent, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(item.screen, style = MaterialTheme.typography.titleMedium, color = view.ink, modifier = Modifier.weight(1f))
        Text(
            item.duration ?: "now",
            style = EventText.Mono11Medium,
            color = view.ink,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
internal fun ScreenBandEnd(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .fillMaxWidth()
            .height(12.dp)
            .background(screenBandColor(isDarkSurface()), BandShapeBottom)
    )
}
```

Previews:
- `ScreenBandHeaderResumedPreview`: `BandHeaderItem("k", "For You (resumed)", startMillis = ViewerSampleData.startMillis + 145_300, duration = null)`.
- `ScreenBandPreview`: header "Stop Details 3.4s" plus three `EventRow(inBand = true, modifier = Modifier.screenBandBackground())` plus the end.

- [ ] **Step 6: `LanesCard.kt`.**

```kotlin
private const val TAP_TOLERANCE_DP = 24f

@Composable
internal fun LanesCard(model: LanesModel, onMarkClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val view = EventType.VIEW.colors()
    val click = EventType.CLICK.colors()
    val other = EventType.OTHER.colors()
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(model.range, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text("${model.span} · follows scroll", style = EventText.Mono11, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(8.dp))
        LaneRow("Screens", view.ink, Lane.SCREENS, model, onMarkClick) { width ->
            model.screens.forEach { bar ->
                val barWidth = width * (bar.end - bar.start) - 2.dp
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(x = width * bar.start)
                        .width(barWidth.coerceAtLeast(2.dp))
                        .height(22.dp)
                        .background(view.tint, RoundedCornerShape(6.dp)),
                ) {
                    if (barWidth >= 44.dp) Text(bar.label, style = MaterialTheme.typography.labelMedium, color = view.ink, maxLines = 1, overflow = TextOverflow.Clip)
                }
            }
        }
        LaneRow("Clicks", click.ink, Lane.CLICKS, model, onMarkClick) { width ->
            model.clicks.forEach { mark ->
                Box(Modifier.offset(x = width * mark.x - 4.5.dp).size(9.dp).rotate(45f).background(click.accent, RoundedCornerShape(1.dp)))
            }
        }
        LaneRow("Events", other.ink, Lane.EVENTS, model, onMarkClick) { width ->
            model.events.forEach { mark ->
                Box(Modifier.offset(x = width * mark.x - 4.dp).size(8.dp).background(other.accent, CircleShape))
            }
        }
        Row(Modifier.padding(start = 50.dp, top = 4.dp)) {
            BoxWithConstraints(Modifier.weight(1f)) {
                model.axis.forEach { (fraction, label) ->
                    Text(label, style = EventText.Mono9, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.offset(x = maxWidth * fraction - 10.dp))
                }
            }
        }
    }
}

@Composable
private fun LaneRow(
    caption: String,
    captionColor: Color,
    lane: Lane,
    model: LanesModel,
    onMarkClick: (String) -> Unit,
    marks: @Composable BoxScope.(width: Dp) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(30.dp)) {
        Text(caption, style = MaterialTheme.typography.labelMedium, color = captionColor, modifier = Modifier.width(50.dp))
        BoxWithConstraints(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(model) {
                    detectTapGestures { offset ->
                        model.markAt(lane, offset.x / size.width, TAP_TOLERANCE_DP.dp.toPx() / size.width)?.let(onMarkClick)
                    }
                },
        ) { marks(maxWidth) }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}
```

Preview: `LanesCardPreview` with `lanesModel(ViewerSampleData.events, start + 1_050, start + 15_000)`.

- [ ] **Step 7: `ViewerChrome.kt`.**

```kotlin
@Composable
internal fun EventSearchField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { field ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text("Filter by name or property", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    field()
                }
            }
        },
    )
}

@Composable
internal fun CopyJsonBar(onCopy: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().navigationBarsPadding()) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        FilledTonalButton(
            onClick = onCopy,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
                .fillMaxWidth()
                .height(40.dp),
        ) {
            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Copy session as JSON")
        }
    }
}
```

Previews: `EventSearchFieldEmptyPreview`, `CopyJsonBarPreview`.

- [ ] **Step 8: Render all component references and compare.** Run `./gradlew :debug-menu:updateDebugScreenshotTest`. Compare each new image with its `$HANDOFF/components/viewer-*.png` counterpart (row collapsed / expanded / no properties, gap marker, session card collapsed / expanded, journey band header, journey lanes, search field, bottom bar). Note every difference that isn't caused by the theme or font, fix what you can, and list the rest for Task 14.
- [ ] **Step 9: Commit.** `git add debug-menu && git commit -m "feat(debug): event viewer components"`

---

### Task 12: Events screen (Timeline + Journey)

**Files:**
- Rewrite: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventLogScreen.kt`
- Modify: `debug-menu/src/main/java/com/sloy/debugmenu/events/EventType.kt` (delete `EventTypeIcon`)

**Interfaces:**
- Consumes: Tasks 7, 8, 11; `EventLogViewModel` (unchanged: `events`, `onClearEvents`).
- Produces: `fun EventLogScreen(eventStore: EventStore, onClose: () -> Unit)` (same public signature); `@Composable internal fun EventLogScreenContent(events: List<CapturedEvent>, onClose: () -> Unit, onClear: () -> Unit, initialView: Int = 0, initiallyExpanded: Set<String> = emptySet(), onCopy: (String, Int) -> Unit = { _, _ -> })`.

- [ ] **Step 1: Write the screen.**

```kotlin
private const val TIMELINE = 0
private const val JOURNEY = 1
private const val LANES_KEY = "lanes"

@Composable
fun EventLogScreen(eventStore: EventStore, onClose: () -> Unit) {
    val viewModel = viewModel { EventLogViewModel(eventStore) }
    val events by viewModel.events.collectAsStateWithLifecycle()
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    EventLogScreenContent(
        events = events,
        onClose = onClose,
        onClear = viewModel::onClearEvents,
        onCopy = { json, _ -> scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Events", json))) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventLogScreenContent(
    events: List<CapturedEvent>,
    onClose: () -> Unit,
    onClear: () -> Unit,
    initialView: Int = TIMELINE,
    initiallyExpanded: Set<String> = emptySet(),
    onCopy: (json: String, count: Int) -> Unit = { _, _ -> },
) {
    var selectedView by rememberSaveable { mutableIntStateOf(initialView) }
    var query by rememberSaveable { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val items = remember(events, query, selectedView) {
        if (selectedView == JOURNEY) journeyItems(events, query) else timelineItems(events, query)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(selectedView) { listState.scrollToItem(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events (${events.size})") },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } },
                actions = {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Clear events", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
            )
        },
        bottomBar = {
            CopyJsonBar(onCopy = {
                onCopy(events.toSessionJson(), events.size)
                scope.launch { snackbar.showSnackbar("Copied ${events.size} events") }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PillSegmentedControl(
                options = listOf("Timeline", "Journey"),
                selectedIndex = selectedView,
                onSelected = { selectedView = it },
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
            )
            EventSearchField(query, { query = it }, Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            when {
                events.isEmpty() -> CenteredMessage("No events yet")
                items.isEmpty() -> CenteredMessage("No matching events")
                else -> EventList(events, items, selectedView == JOURNEY, listState, initiallyExpanded)
            }
        }
    }
}

@Composable
private fun ColumnScope.CenteredMessage(text: String) {
    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EventList(
    events: List<CapturedEvent>,
    items: List<ViewerItem>,
    showLanes: Boolean,
    listState: LazyListState,
    initiallyExpanded: Set<String>,
) {
    val scope = rememberCoroutineScope()
    val eventsById = remember(events) { events.associateBy { it.id } }
    val visibleRange by remember(items, eventsById) {
        derivedStateOf {
            val timestamps = listState.layoutInfo.visibleItemsInfo.mapNotNull { eventsById[it.key]?.timestampMillis }
            if (timestamps.isEmpty()) null else timestamps.min() to timestamps.max()
        }
    }
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (showLanes) {
            item(key = LANES_KEY) {
                val (from, to) = visibleRange ?: (events.minOf { it.timestampMillis } to events.maxOf { it.timestampMillis })
                LanesCard(
                    model = remember(events, from, to) { lanesModel(events, from, to) },
                    onMarkClick = { id ->
                        val index = items.indexOfFirst { it.key == id }
                        if (index >= 0) scope.launch { listState.animateScrollToItem(index + 1) }
                    },
                )
            }
        }
        items(items, key = { it.key }) { item ->
            when (item) {
                is EventItem -> {
                    var expanded by rememberSaveable(item.key) { mutableStateOf(item.key in initiallyExpanded) }
                    EventRow(
                        event = item.event,
                        delta = item.delta,
                        expanded = expanded,
                        onToggle = { expanded = !expanded },
                        inBand = item.inBand,
                        modifier = if (item.inBand) Modifier.screenBandBackground() else Modifier,
                    )
                }
                is SessionItem -> {
                    var expanded by rememberSaveable(item.key) { mutableStateOf(item.key in initiallyExpanded) }
                    SessionSummaryCard(item.event, expanded, onToggle = { expanded = !expanded })
                }
                is GapItem -> GapMarker(item.label)
                is BandHeaderItem -> ScreenBandHeader(item)
                is BandEndItem -> ScreenBandEnd()
            }
        }
    }
}
```

`LocalClipboard` and `ClipEntry` come from `androidx.compose.ui.platform`; `ClipData` comes from `android.content`. Expanded state lives in `rememberSaveable` per item key, which the `LazyColumn` keeps across scrolling and configuration changes (the current behaviour). Delete `EventTypeIcon` and its imports from `EventType.kt`.

- [ ] **Step 2: Previews** (Screens suite):

```kotlin
@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenTimelinePreview() {
    DebugPreviewTheme {
        EventLogScreenContent(ViewerSampleData.events, {}, {}, initiallyExpanded = setOf(ViewerSampleData.expandedEventId))
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenJourneyPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(ViewerSampleData.events, {}, {}, initialView = 1, initiallyExpanded = setOf(ViewerSampleData.expandedEventId))
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenSessionExpandedPreview() {
    DebugPreviewTheme {
        EventLogScreenContent(
            ViewerSampleData.events, {}, {},
            initiallyExpanded = setOf(ViewerSampleData.expandedEventId, ViewerSampleData.sessionSummaryId),
        )
    }
}

@ScreenshotTest(ScreenshotSuite.Screens)
@PreviewLightDark
@Composable
internal fun EventLogScreenEmptyPreview() {
    DebugPreviewTheme { EventLogScreenContent(emptyList(), {}, {}) }
}
```

Remove the old private previews.

- [ ] **Step 3: Build and run the unit tests.** Run `./gradlew :debug-menu:testDebugUnitTest :app:assembleDebug`. Expected: PASS.
- [ ] **Step 4: Render and compare.** Run `./gradlew :debug-menu:updateDebugScreenshotTest` and compare:
  - Timeline light/dark with `$HANDOFF/screenshots/viewer-timeline-light.png` / `-dark.png`.
  - Journey with `viewer-journey-*.png`.
  - Session expanded with `viewer-timeline-*-session-expanded.png`.
  - Empty with `viewer-empty-light.png`.

  Only the top of each screen fits the default preview device. That's expected; the components cover the rest.
- [ ] **Step 5: Commit.** `git add debug-menu && git commit -m "feat(debug): events screen with timeline and journey views"`

---

### Task 13: Docs

**Files:**
- Modify: `CLAUDE.md` (Debug Menu section)

- [ ] **Step 1:** In "Debug Menu":
  - Change "`DebugMenuHost` wraps the app content in `App.kt`. It draws the overlay pills…" to also mention the `overlay` slot (`SevDebugOverlay`, anchored `overlayBottomPadding` above the safe drawing bottom).
  - Change the `EventsModule` description to: event overlay (spring stack, or timeline rail with "Use timeline rail") and a full-screen Events screen (Timeline/Journey, search, copy as JSON).
  - Add to the `:debug-menu-noop` sentence that `DebugMenuHost` mirrors the new parameters.
  - Add a line saying the analytics facade delivers events to trackers in call order (in the Analytics "Core Architecture" list: "Tracking calls go through one FIFO queue, so trackers receive events in call order").
- [ ] **Step 2: Commit.** `git commit -am "docs: debug events overlay and viewer"`

---

### Task 14: Verification

- [ ] **Step 1: Full checks.** Run `./gradlew test lint`. Expected: BUILD SUCCESSFUL. Fix every new lint issue in touched files.
- [ ] **Step 2: Screenshots.** Run `./gradlew validateDebugScreenshotTest` (both modules) and `./gradlew -p build-logic test`. Expected: PASS, no orphan references.
- [ ] **Step 3: Release.** Run `./gradlew :app:compileReleaseKotlin`. Expected: PASS (the noop mirrors the API).
- [ ] **Step 4: Side-by-side comparison.** For each preview → reference pair in the docs/09 "Previews to compare" table (plus the components), put the rendered PNG and the handoff PNG side by side. For example, build a montage with `python3 -I` + Pillow into the scratchpad if available, otherwise read both images. Record every difference in the backlog task notes, classified as:
  - intended: theme or font (stock M3 instead of SevTheme, no Geist), no map/blur, preview height;
  - fixed;
  - unresolved, with the reason.
- [ ] **Step 5: Device check (if an emulator is available through `mcp__maestro__list_devices`).**
  - Install the debug build and enable "Show events on overlay".
  - Navigate: map → stop → back → favorites.
  - Screenshot the stack and the rail.
  - Tune `DebugOverlayBottomPadding` in `App.kt` so the stack's bottom clears the navigation bar by about 20 dp.
  - Open Events → Journey, tap a lane mark, copy JSON.

  If no device is available, say so in the notes.
- [ ] **Step 6: Finalize the backlog task.** Follow `backlog instructions task-finalization`: check each AC with its evidence and write the final summary.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Task 1 done: debug-menu screenshot tests via plugin (library modules supported with HasHostTests), CI/report script multi-module. app+debug-menu validate pass.

Task 2 done: CapturedEvent.timestampMillis, EventFormat, EventsJson, OverlayTracker clock; tests pass.

Task 3 done: Analytics uses a FIFO channel with one consumer. Test uses advanceTimeBy+runCurrent instead of advanceUntilIdle because advanceUntilIdle skips backgroundScope tasks; expected values unchanged.

Task 4 done: accent tokens, EventText, OverlayStyle; EventTypeTest extended.

Task 5 done: StackLayout + OverlayDemo, 15 tests pass.

Task 6 done: RailLayout, 12 tests pass.

Decision (Opus): sample deltas 615ms/15ms conflicted with half-up formatDelta (design used JS float toFixed) -> shift ViewerSampleData Stop Details Viewed timestamps to 4_254 and 9_336, keep formatDelta and test expectations, because no integer-ms rounding rule satisfies both references.

Tasks 7+8 done (one commit): timeline/journey items, search, lanes; 27 viewer tests pass.

Task 9 done. Visual vs overlay-stack-t10.6s.png: same chip order, x2/x3 badges, 3/9dp gaps, fade, timers. Intended diffs: no map/blur, stock monospace font, 844dp preview.

Task 9b done. Rail vs overlay-rail-push-t10.6s.png: same markers/labels/bands/links/ticks. Intended diffs: no map/blur, stock primary now head, font, strip uses raw frame values (no 250ms push animation for bands/links).

Task 10 done: EventsOverlay, rail checkbox (hidden when overlay off), host slot + noop mirror, app wiring (DebugOverlayBottomPadding=100dp, to tune in Task 14). Unit tests, assembleDebug, compileReleaseKotlin pass.

Task 11 done. Compared 11 component previews with handoff viewer-*.png. Fixed: inline properties lost 2nd line/+N more (FlowRow in IntrinsicSize.Min) -> single wrapping Text; session icon spot now neutral like reference. Intended: stock M3 colours/fonts, preview width (lane bar labels clip earlier), outline colours. Unresolved: lanes axis labels use the padded window (00s..15s) while the design labels the unpadded range (01s..15s); mine is geometrically aligned with the marks.

Task 12 done. Timeline/Journey screens compared with viewer-*-light.png: same topbar, segmented control, search, rows, gap, session card, bands, pinned copy bar. Intended: stock theme/fonts, preview height cuts the list. Note: in Journey preview the lanes card spans 16:21:01-16:23:25 because the resumed band is visible at the top (follows scroll); the reference screenshot was scrolled.

Task 13 done: CLAUDE.md updated.

Verification (Task 14): ./gradlew test PASS; validateDebugScreenshotTest PASS (app + debug-menu, 48 debug-menu screenshot tests, no orphans); -p build-logic test PASS; :app:compileReleaseKotlin PASS; assembleDebug PASS. ./gradlew lint FAILS on app/src/main/res/values/themes.xml:8 NewApi windowSplashScreenBackground (file identical to master, pre-existing, not touched). Device check NOT run: only a real phone is connected over wireless adb, no emulator, so nothing was installed. DebugOverlayBottomPadding stays at the untuned 100dp. Unchecked ACs (not proven without a device or UI test): #4 checkbox hidden/ clear drops chips, #7 touch/accessibility/anchor, #9 expansion survives config change, #10 and #11 tap-to-scroll, #12 clipboard and snackbar, #16 lint. Visual differences summary: intended = stock M3 theme/fonts, no map/blur, preview height; fixed = inline properties wrap, session icon spot; unresolved = lanes axis labels use padded window (design uses unpadded range), rail bands/links skip the 250ms push animation.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Implemented the spring stack and timeline rail overlays, the Timeline/Journey Events screen, JSON export, FIFO analytics delivery and debug-menu screenshot tests in CI. Verified with unit tests, validateDebugScreenshotTest for both modules, build-logic tests and the release compile. Not verified: lint (pre-existing themes.xml error) and on-device behaviour.
<!-- SECTION:FINAL_SUMMARY:END -->
