# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Building
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew installDebug` - Install debug build on connected device

### Testing and Quality
- `./gradlew test` - Run all unit tests
- `./gradlew check` - Run all checks (tests + lint)
- `./gradlew lint` - Run lint analysis
- `./gradlew lintFix` - Run lint and apply safe fixes
- `./gradlew updateDebugScreenshotTest` - Generate/update screenshot test references
- `./gradlew validateDebugScreenshotTest` - Validate screenshots against references

### Clean
- `./gradlew clean` - Clean build directory

## Deployment

### Standard Deployment (via CI)

Release to Google Play via GitHub Actions workflow:

1. Navigate to **Actions** → **Create Release** in GitHub
2. Click **Run workflow** and select:
   - **Release type**: `patch`, `minor`, or `major`
   - **Release name** (optional): e.g., "Bonobús alerts"
3. The workflow will:
   - Update version in `version.properties` (removes `-snapshot`)
   - Create a git tag (e.g., `v5.5.1`)
   - Generate release notes from commits
   - Create a GitHub release
   - Build signed AAB and deploy to Google Play Internal Track
   - Prepare next development version (bumps patch and adds `-snapshot`)

### Manual Deployment (CI unavailable)

If GitHub Actions is unavailable, deploy manually:

1. **Prepare release version:**
   ```bash
   ./scripts/prepare-release.sh patch  # or minor/major
   git add version.properties
   git commit -m "chore: release v$(source version.properties && echo ${major}.${minor}.${patch})"
   source version.properties
   git tag "v${major}.${minor}.${patch}"
   git push origin "v${major}.${minor}.${patch}"
   ```

2. **Build signed AAB:**
   ```bash
   ./gradlew :app:bundleRelease
   ```
   Requires:
   - `secret.properties` with `MAPS_API_KEY`
   - `app/google-services.json`
   - `certs/release.keystore` file
   - Environment variables: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

3. **Upload to Google Play Console:**
   - Go to [Google Play Console](https://play.google.com/console)
   - Navigate to your app → Internal testing
   - Upload `app/build/outputs/bundle/release/app-release.aab`
   - Upload ProGuard mapping: `app/build/outputs/mapping/release/mapping.txt`

4. **Prepare next development version:**
   ```bash
   ./scripts/prepare-next-dev.sh
   git add version.properties
   git commit -m "chore: prepare next development iteration $(source version.properties && echo ${major}.${minor}.${patch}-snapshot)"
   git push origin master
   ```

## Architecture Overview

SeviBus follows Modern Android Development practices with Clean Architecture:

### Package Structure
- `ui/` - Jetpack Compose UI components, themes, and reusable widgets
- `feature/` - Feature modules (cards, foryou, lines, map, search, stopdetail, etc.)
- `domain/` - Business logic, use cases, and domain models  
- `data/` - Repositories, API clients, database, and caching
- `infrastructure/` - App-level concerns (DI, location services, NFC, session management, polyline encoding)
- `navigation/` - Navigation components and routing

### Key Technologies
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture
- **DI**: Koin
- **Async**: Kotlin Coroutines and Flow
- **Database**: Room
- **Network**: Retrofit + OkHttp with Kotlinx Serialization
- **Maps**: Google Maps
- **Authentication**: Firebase Auth with Google Sign-In
- **Analytics**: Firebase (Analytics, Crashlytics, Performance), Amplitude, Statsig
- **NFC**: Custom NFC implementation for Bonobús cards

### Configuration Requirements
- JDK 21
- `secret.properties` file in project root with `MAPS_API_KEY`
- `app/google-services.json` for Firebase services
- `version.properties` file in project root (managed by release scripts)

### Build Configuration
- Compile SDK: 36, Target SDK: 36, Min SDK: 26
- Kotlin with JVM target 21
- ProGuard enabled for release builds with optimization
- Signing configs: Release (uses `certs/release.keystore`) and Debug (uses `certs/debug.keystore`)
- KSP for Room code generation
- Room schemas stored in `app/schemas/`
- Version management via `version.properties` (major.minor.patch format)

### Testing
- Unit tests: JUnit, Strikt assertions, Mockito-Kotlin, Coroutines testing
- Instrumentation tests available via `connectedAndroidTest`
- Screenshot tests: Compose Preview Screenshot Testing (see below)
- Debug builds include:
  - Chucker for network debugging
  - Debug menu module (`:debug-menu`) for development tools
  - No-op implementation for release builds

## Debug Menu

Debug tooling lives in `:debug-menu` (debug builds) and `:debug-menu-noop` (release builds), wired with `debugImplementation` / `releaseImplementation`.

- `:debug-menu` is self-contained: no `com.sloy.sevibus` imports, no app resources, no Koin. It only uses `MaterialTheme` tokens, so it follows `SevTheme` automatically.
- `:debug-menu-noop` mirrors only the API used from `app/src/main`: `DebugMenuHost`, `DebugMenuScope`, `OverlayLogger`, `OverlayLoggerItem`.
- `DebugMenuHost` wraps the app content in `App.kt`. It draws the overlay pills, a draggable floating button and the menu `ModalBottomSheet`. `DebugMenuScope.openScreen` shows full-screen debug screens.
- Library sections: `NetworkModule` (HTTP overlay, forced failure, latency, API host override with QR scan) and `EventsModule` (event overlay and full-screen event log).
- App sections live in `app/src/debug/java/com/sloy/sevibus/feature/debug/` and are composed in `SevDebugMenu`. Release has an empty `SevDebugMenu`.

### Adding a section

1. Create `@Composable fun DebugMenuScope.MySection()` using `DebugModule`, `DebugCell`, `TitleSubtitle` and `PillSegmentedControl`.
2. Persist its state with a `DebugModuleDataSource<MyState>` subclass and a `@Serializable` state class.
3. Give it a ViewModel when it has state or actions. App sections get theirs with `koinViewModel()`; library sections create theirs with `viewModel { }` from the dependencies passed in.
4. Render a stateless private content composable when `LocalInspectionMode.current` is true so previews work without DI.
5. Add it to `SevDebugMenu` and bind its dependencies in the debug `DebugDI`.

## Screenshot Testing

SeviBus uses [Compose Preview Screenshot Testing](https://developer.android.com/studio/preview/compose-screenshot-testing) (experimental) to automatically generate and validate screenshots of Compose previews.

### Overview

The plugin only discovers `@PreviewTest` functions in the `screenshotTest` source set, so the tests are **generated wrappers** around the previews in main:

- **Preview functions** live in `app/src/main/` with `internal` visibility and are annotated with `@ScreenshotTest(ScreenshotSuite.X)`
- **Test wrappers** are generated by the `generateDebugScreenshotTests` task (`buildSrc/src/main/kotlin/GenerateScreenshotTestsTask.kt`) into `app/build/generated/`. Never write them by hand
  - It reads the main Kotlin sources, so it doesn't depend on compiling main
  - Only the screenshotTest compilation depends on it: `assembleDebug`, `installDebug` or `test` don't run it
  - Each wrapper is named after its preview and calls it with `@Preview(locale = "es")` and `@PreviewTest`
  - Previews with `@PreviewLightDark` or a `@Preview(uiMode = UI_MODE_NIGHT_YES)` also get a dark `@Preview`, so they produce a light and a dark screenshot
  - The build fails if an annotated preview is `private`, has parameters, or two previews in a suite share a name
- Tests are grouped in two classes, which act as suites:
  - `ComponentsScreenshotTests` - reusable components and screen sections (widgets, list items, icons)
  - `ScreensScreenshotTests` - full screens
- The suite is a safety net, not full coverage: add a test only for meaningful states
- **Reference screenshots** are stored in `app/src/screenshotTestDebug/reference/`

### Commands

```bash
# Generate/update reference screenshots (run after creating new tests or changing UI)
./gradlew updateDebugScreenshotTest

# Validate screenshots against references (run to check for visual regressions)
./gradlew validateDebugScreenshotTest

# Run or update a single suite or test
./gradlew validateDebugScreenshotTest --tests '*ComponentsScreenshotTests'
./gradlew updateDebugScreenshotTest --tests '*ScreensScreenshotTests.ForYouScreenPreview'
```

The HTML report with reference, rendered and diff images is in `app/build/reports/screenshotTest/preview/debug/`.

### Adding New Screenshot Tests

Follow these steps to add screenshot tests for a component:

#### 1. Create an Annotated Preview in Main Source

In your component file (e.g., `app/src/main/java/com/sloy/sevibus/ui/components/MyComponent.kt`):

```kotlin
@ScreenshotTest(ScreenshotSuite.Components) // or ScreenshotSuite.Screens for full screens
@Preview
@Composable
internal fun MyComponentDefaultPreview() {
    SevTheme {
        MyComponent(/* deterministic test data */)
    }
}
```

**Important:**
- Use `internal` visibility (not `private`) so the generated wrappers can call them
- The preview name becomes the test name and screenshot file name. Renaming a preview renames its reference image
- Name previews `<ScreenName><Scenario>Preview` for screens (e.g. `StopDetailScreenFailedArrivalsPreview`) and `<ComponentName><Scenario>Preview` for components (e.g. `AppUpdateButtonReadyPreview`), so names are unique across packages and identifiable on their own
- Use **deterministic test data** (no `.random()`, `.shuffled()`, `Random.nextInt()`, etc.)
- Wrap in `SevTheme` for consistent theming
- Only day/night is picked up from the main preview (`@PreviewLightDark` or a night `uiMode`). Other `@Preview` parameters are ignored, the generated wrapper's `@Preview(locale = "es")` decides the configuration

#### 2. Generate Reference Screenshots

```bash
./gradlew updateDebugScreenshotTest
```

This creates PNG files in `app/src/screenshotTestDebug/reference/com/sloy/sevibus/ComponentsScreenshotTests/`. Non-default `@Preview` parameters add a hash to the file name, e.g. `MyComponentDefaultPreview_b2db1d68_0.png` for light and `MyComponentDefaultPreview_f6f1fda3_0.png` for dark.

#### 3. Validate Screenshots

```bash
./gradlew validateDebugScreenshotTest
```

All tests should pass on first generation. Commit the reference screenshots to git.

### Deterministic Test Data

**Critical:** Screenshot tests require deterministic data to produce consistent results across test runs.

**When using Stubs:**
- The `Stubs` object has been refactored to return deterministic data
- Use fixed indices: `Stubs.lines[0]`, `Stubs.stops[1]`, etc.
- Avoid `.random()`, `.shuffled()`, `Random.nextInt()`, `Random.nextLong()`, etc.
- Avoid `LocalDateTime.now()` and other clock reads, use fixed dates
- Cover different cases with different test previews (e.g., empty state, single item, multiple items)

### Testing Different States

Create separate preview functions for different component states:

```kotlin
// Empty state
@Preview
@Composable
internal fun MyComponentEmptyPreview() {
    SevTheme {
        MyComponent(items = emptyList())
    }
}

// Single item
@Preview
@Composable
internal fun MyComponentSingleItemPreview() {
    SevTheme {
        MyComponent(items = listOf(Stubs.items[0]))
    }
}

// Multiple items
@Preview
@Composable
internal fun MyComponentMultipleItemsPreview() {
    SevTheme {
        MyComponent(items = listOf(Stubs.items[0], Stubs.items[1], Stubs.items[2]))
    }
}

// Error state
@Preview
@Composable
internal fun MyComponentErrorPreview() {
    SevTheme {
        MyComponent(error = "Something went wrong")
    }
}
```

### Configuration

Screenshot testing is configured in:

**gradle.properties:**
```properties
android.experimental.enableScreenshotTest=true
```

**gradle/libs.versions.toml:**
```toml
[versions]
composeScreenshot = "0.0.1-alpha16"

[libraries]
compose-screenshot-validation = { module = "com.android.tools.screenshot:screenshot-validation-api", version.ref = "composeScreenshot" }

[plugins]
compose-screenshot = { id = "com.android.compose.screenshot", version.ref = "composeScreenshot" }
```

**app/build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.compose.screenshot)
}

android {
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

// No DSL for the threshold since AGP 9, so it's set on the validation task
tasks.withType<PreviewScreenshotValidationTask>().configureEach {
    testEngineInput.threshold.set(0.01f)  // 1% tolerance for image differences
}

dependencies {
    screenshotTestImplementation(libs.compose.screenshot.validation)
    screenshotTestImplementation(libs.androidx.ui.tooling)
    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.androidx.ui)
}
```

### Troubleshooting

**Issue: Tests fail with image differences**
- Run `./gradlew updateDebugScreenshotTest` to regenerate references
- Check if you're using non-deterministic data (random values, timestamps, etc.)
- Verify that `SevTheme` is applied consistently

**Issue: `@Preview annotation is required for @PreviewTest`**
- Both annotations must be present on the test wrapper function

**Issue: Cannot find preview function**
- Ensure preview function is `internal` (not `private`) and annotated with `@ScreenshotTest`
- Check the generated wrappers in `app/build/generated/` after running `./gradlew generateDebugScreenshotTests`

**Issue: `Resources_Delegate.initSystem called twice before disposeSystem was called`**
- Layoutlib failure on the test rendered *after* a preview that doesn't release its render session, like a Lottie animation (`cardsEmptyNfcEnabled`). Check the log for the test rendered just before and keep that preview out of the suite

**Issue: Screenshots look different on different machines**
- Ensure deterministic test data (no random values)
- Check that all developers use the same JDK version (21)
- Verify image difference threshold in `build.gradle.kts`

### Directory Structure

```
app/src/
├── main/
│   └── java/com/sloy/sevibus/ui/components/
│       └── MyComponent.kt          # Component + @ScreenshotTest @Preview functions (internal)
├── screenshotTest/
│   └── AndroidManifest.xml         # Required empty manifest, the test wrappers are generated
└── screenshotTestDebug/
    └── reference/com/sloy/sevibus/
        ├── ComponentsScreenshotTests/
        │   └── MyComponentDefaultPreview_b2db1d68_0.png   # Reference screenshots (commit to git)
        └── ScreensScreenshotTests/
```

### CI

`.github/workflows/screenshot-tests.yml` validates the screenshots on every pull request and every push to master.

On pull requests, failures are reported in a single PR comment (updated on each run) with the reference, new and diff images. The images are pushed to a `screenshots/pr-<number>` companion branch, deleted when the PR is closed. Add the `update-screenshots` label to regenerate the references of the failing tests, commit them to the PR branch and remove the label. Use it instead of running `updateDebugScreenshotTest` locally. Commits pushed by the workflow don't trigger new runs, so push again to validate them. PRs from forks are skipped, since the workflow needs write access.

On pushes to master, if any screenshot fails, it regenerates the failing references on the `screenshots/master-update` branch and opens a PR assigned to the pusher (or refreshes the open one and comments on it). Merge it if the changes are expected, otherwise close it and fix the UI. When master passes again, an open update PR is closed. Opening PRs requires **Settings → Actions → General → Allow GitHub Actions to create and approve pull requests**.

The comment is built by `.github/scripts/screenshot_report.py` from the JUnit results. The full HTML report is also uploaded as the `screenshot-report` artifact.

### Key Files Reference

- **Plugin configuration**: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- **Test data**: `app/src/main/java/com/sloy/sevibus/Stubs.kt` (deterministic test data)
- **Annotation**: `app/src/main/java/com/sloy/sevibus/ui/preview/ScreenshotTest.kt`
- **Test generator**: `buildSrc/src/main/kotlin/GenerateScreenshotTestsTask.kt`, wired in `app/build.gradle.kts`
- **Reference screenshots**: `app/src/screenshotTestDebug/reference/`

## Analytics & Event Tracking

SeviBus uses a multi-tracker analytics system with a type-safe event model.

### Analytics Services

- **Amplitude** - Main analytics service with session tracking, frustration detection, and deep links
- **Firebase Analytics** - Fallback analytics service for basic event tracking
- **HappyMomentTracker** - Internal tracker for triggering in-app review prompts based on user behavior
- **LoggerTracker** - Development-only tracker that logs events to Logcat
- **OverlayTracker** - Debug-only tracker that displays events in an on-screen overlay (debug builds only)

### Core Architecture

**Main Entry Point:** `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/Analytics.kt`

- Single facade for all event tracking
- Respects user opt-in/opt-out preference (defaults to enabled)
- Uses Kotlin Coroutines with `Dispatchers.Default` for async, non-blocking tracking
- Broadcasts events to all registered tracker implementations

**Base Event Model:** `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/SevEvent.kt`

```kotlin
abstract class SevEvent(
   val name: String,
   vararg val properties: Pair<String, Any?>
)
```

### Adding New Events

#### 1. Define the Event

Events are organized into three files based on type:

- **`events/Screens.kt`** - For screen view tracking (use "Viewed" suffix)
- **`events/Clicks.kt`** - For user interactions (use "Clicked" suffix)
- **`events/Events.kt`** - For general events (no strict suffix)

**Example:**

```kotlin
// In events/Clicks.kt
interface Clicks {
   // Event without properties
   data object EditFavoritesClicked : SevEvent(
      "Edit Favorites Clicked"
   )

   // Event with properties
   data class EditFavoriteLineClicked(val isSelected: Boolean) : SevEvent(
      "Edit Favorite Line Clicked",
      "lineId" to lineId,
      "isSelected" to isSelected
   )
}
```

#### 2. Naming Conventions

- **Event Names**: Use Title Case with spaces (e.g., "Add Favorite Clicked")
   - Firebase automatically converts to underscores: `Add_Favorite_Clicked`
   - The event naming follows the structure "Object + Action"
- **Screen Events**: Use "[Screen Name] Viewed" pattern
   - Examples: "For You Viewed", "Stop Details Viewed", "Settings Viewed"
- **Click Events**: Use "[Action] Clicked" pattern
   - Examples: "Add Favorite Clicked", "Card Top Up Clicked", "Location Button Clicked"
- **General Events**: Use descriptive past tense or noun phrases
   - Examples: "App Started", "Card Alert Displayed", "Review Dialog Requested"

#### 3. Event Properties

- Use **camelCase** for property keys (e.g., `stopId`, `lineLabel`, `balanceType`)
- Supported types: `String`, `Int`, `Long`, `Double`, `Float`, `Boolean`
   - `Int` → converted to `Long` in Firebase
   - `Float` → converted to `Double` in Firebase
   - `null` values are skipped in Firebase
- Keep property names concise but descriptive
- Include context needed for analytics (IDs, states, types, counts)

#### 4. Where to Track Events

**Primary: ViewModels** (Preferred)

- Inject `Analytics` via Koin
- Track business logic events and user actions
- Track when state changes occur

```kotlin
class MyViewModel(
   private val analytics: Analytics,
) : ViewModel() {

   fun onButtonClick() = viewModelScope.launch {
      doSomething()
      analytics.track(Clicks.ButtonClicked)
   }
}
```

**Secondary: Composable UI**

- Use `koinInjectOnUI<Analytics>()` for nullable injection
- Track UI-specific interactions that don't go through ViewModel
- Use `LaunchedEffect` for tracking state-based events

```kotlin
@Composable
fun MyScreen() {
   val analytics: Analytics? = koinInjectOnUI()

   Button(
      onClick = {
         onNavigate(destination)
         analytics?.track(Clicks.NavigateClicked)
      }
   ) { Text("Navigate") }
}
```

**Automatic: Navigation**

- Screen views are automatically tracked via `AppState` in `App.kt`
- Define screen event in `events/Screens.kt`
- Add mapping in `Screens.kt` helper function:

```kotlin
fun Analytics.track(destination: NavigationDestination) {
   val event = when (destination) {
      is NavigationDestination.MyNewScreen -> Screens.MyNewScreenViewed
      // ... other mappings
   }
   track(event)
}
```

### Privacy & User Consent

- Analytics is **enabled by default** (opt-out model)
- Users can disable from **Settings → Analytics**
- All trackers respect the user preference in real-time
- Preference is stored in DataStore: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/AnalyticsSettingsDataSource.kt`
- **Never track personally identifiable information (PII)** without explicit user consent

### Testing

- Events are logged to Logcat in debug builds via `LoggerTracker`
- Check Logcat with filter: `"Tracked event"` to verify events are firing
- Analytics preference changes can be tested via the Settings screen
- Mock `Analytics` in unit tests to verify tracking calls:

```kotlin
@Test
fun `should track event when button clicked`() {
   val analytics = mock<Analytics>()
   val viewModel = MyViewModel(analytics)

   viewModel.onButtonClick()

   verify(analytics).track(Clicks.ButtonClicked)
}
```

### Best Practices

1. **Track user intent, not implementation details**
   - ✅ Good: `track(Clicks.AddFavoriteClicked(stopId))`
   - ❌ Bad: `track(Events.DatabaseInsertCompleted)`

2. **Keep events simple and focused**
   - One event per user action or state change
   - Avoid overly granular tracking (e.g., don't track every scroll position)

3. **Use strong typing**
   - Define events as data classes with typed properties
   - Avoid magic strings for event names or property keys

4. **Be consistent with naming**
   - Follow existing patterns in `events/` folder
   - Use the same terminology across similar events

5. **Track early in the flow**
   - Track user interactions immediately when they happen
   - Don't wait for async operations to complete (track intent, not outcome)
   - For outcomes, create separate events (e.g., "Operation Succeeded"/"Operation Failed")

6. **Avoid tracking sensitive data**
   - Never track passwords, tokens, email addresses, or phone numbers
   - Use IDs and codes instead of user names or personal information

### Key Files Reference

- **Analytics facade**: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/Analytics.kt`
- **Event definitions**: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/events/`
   - `Screens.kt` - Screen view events
   - `Clicks.kt` - User interaction events
   - `Events.kt` - General events
- **Base event class**: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/SevEvent.kt`
- **Tracker implementations**: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/tracker/`
- **DI configuration**: `app/src/main/java/com/sloy/sevibus/infrastructure/DI.kt`
- **User preferences**: `app/src/main/java/com/sloy/sevibus/infrastructure/analytics/AnalyticsSettingsDataSource.kt`

## Commits

When writing a commit, summarize the given diffs into a concise commit message.
Focus on specific changes.
Do NOT output names, e-mail addresses, or any other personally identifiable information if they are not explicitly in the diffs.

Use "conventional commits" syntax:
<type>(<optional scope>): <description>
empty line as separator
<optional body>

Where the type can be,

- feat: for new features visible to the user
- refactor: for code refactors that don't impact the behaviour of the app
- fix: for fixing bugs or broken behaviour
- ci: for changes related to the CI infrastructure
- chore: for miscelaneous commits like updating dependencies, updating build configuration
- test: for commits that only add or modify tests

The scope is optional. Only add it when the commit is limited to a very specific domain area, for instance, chore(ci) or feat(favorites). Do
not add it if the title is intuitive enough

<!-- BACKLOG.MD GUIDELINES START -->
<!-- backlog.md-instructions-version: 1.53.0 -->
<CRITICAL_INSTRUCTION>

## Backlog.md Workflow

This project uses Backlog.md for task and project management.

**At the beginning of each conversation in this project, run `backlog instructions overview` before answering or taking action. Re-read it only if you have not read it yet in the current conversation.**

Use the overview to decide whether to search, read, create, or update Backlog tasks.

Before task lifecycle actions, read the matching detailed guide:
- `backlog instructions task-creation` before creating or splitting tasks
- `backlog instructions task-execution` before planning, changing status or assignee, adding a plan or implementation notes, or implementing task work
- `backlog instructions task-finalization` before checking acceptance criteria, writing final summaries, or moving tasks to terminal statuses

Use `backlog <command> --help` before running unfamiliar commands. Help shows options, fields, and examples.

Do not edit Backlog task, draft, document, decision, or milestone markdown files directly. Use the `backlog` CLI so metadata, relationships, and history stay consistent.

</CRITICAL_INSTRUCTION>
<!-- BACKLOG.MD GUIDELINES END -->
