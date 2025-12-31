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

## Screenshot Testing

SeviBus uses [Compose Preview Screenshot Testing](https://developer.android.com/studio/preview/compose-screenshot-testing) (experimental) to automatically generate and validate screenshots of Compose previews.

### Overview

The screenshot testing setup uses a **wrapper pattern** to maintain IDE preview visibility while satisfying the `screenshotTest` source set requirement:

- **Preview functions** live in `app/src/main/` with `internal` visibility (visible in IDE)
- **Test wrappers** live in `app/src/screenshotTest/` and call the preview functions with `@PreviewTest` annotation
- **Reference screenshots** are stored in `app/src/screenshotTestDebug/reference/`

### Commands

```bash
# Generate/update reference screenshots (run after creating new tests or changing UI)
./gradlew updateDebugScreenshotTest

# Validate screenshots against references (run to check for visual regressions)
./gradlew validateDebugScreenshotTest
```

### Adding New Screenshot Tests

Follow these steps to add screenshot tests for a component:

#### 1. Create Preview Functions in Main Source

In your component file (e.g., `app/src/main/java/com/sloy/sevibus/ui/components/MyComponent.kt`):

```kotlin
@Preview
@Composable
internal fun MyComponentDefaultPreview() {
    SevTheme {
        MyComponent(/* deterministic test data */)
    }
}
```

**Important:**
- Use `internal` visibility (not `private`) so the screenshotTest source set can access them
- Use descriptive, unique names ending with "Preview"
- Use **deterministic test data** (no `.random()`, `.shuffled()`, `Random.nextInt()`, etc.)
- Wrap in `SevTheme` for consistent theming
- Use `@PreviewLightDark` to test both light and dark themes automatically

#### 2. Create Test Wrapper in screenshotTest Source Set

Create `app/src/screenshotTest/kotlin/com/sloy/sevibus/ui/components/MyComponentScreenshotTests.kt`:

```kotlin
package com.sloy.sevibus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest

/**
 * Screenshot tests for MyComponent.
 * These tests reference preview functions defined in the main source set.
 */
class MyComponentScreenshotTests {

    @Preview
    @PreviewTest
    @Composable
    fun defaultPreview() {
        MyComponentDefaultPreview()
    }

    @Preview
    @PreviewTest
    @Composable
    fun darkModePreview() {
        MyComponentDarkModePreview()
    }
}
```

**Important:**
- Both `@Preview` and `@PreviewTest` annotations are required
- Test function names should be descriptive (they become part of the screenshot filename)
- Use same package as the component for easier access to internal functions

#### 3. Generate Reference Screenshots

```bash
./gradlew updateDebugScreenshotTest
```

This creates PNG files in `app/src/screenshotTestDebug/reference/com/sloy/sevibus/ui/components/MyComponentScreenshotTests/`

#### 4. Validate Screenshots

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
composeScreenshot = "0.0.1-alpha12"

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

    testOptions {
        screenshotTests {
            imageDifferenceThreshold = 0.01f  // 1% tolerance for image differences
        }
    }
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
- Ensure preview function is `internal` (not `private`)
- Verify package name matches between main source and screenshotTest

**Issue: Screenshots look different on different machines**
- Ensure deterministic test data (no random values)
- Check that all developers use the same JDK version (21)
- Verify image difference threshold in `build.gradle.kts`

### Directory Structure

```
app/src/
├── main/
│   └── java/com/sloy/sevibus/ui/components/
│       └── MyComponent.kt          # Component + @Preview functions (internal)
├── screenshotTest/
│   ├── AndroidManifest.xml         # Required empty manifest
│   └── kotlin/com/sloy/sevibus/ui/components/
│       └── MyComponentScreenshotTests.kt  # @PreviewTest wrappers
└── screenshotTestDebug/
    └── reference/
        └── com/sloy/sevibus/ui/components/MyComponentScreenshotTests/
            ├── defaultPreview_0.png
            └── darkModePreview_0.png    # Reference screenshots (commit to git)
```
### Key Files Reference

- **Plugin configuration**: `gradle/libs.versions.toml`, `app/build.gradle.kts`
- **Test data**: `app/src/main/java/com/sloy/sevibus/Stubs.kt` (deterministic test data)
- **Example tests**: `app/src/screenshotTest/kotlin/com/sloy/sevibus/ui/components/`
  - `BusArrivalListItemScreenshotTests.kt` - Complex component with 6 states
  - `InfoBannerComponentScreenshotTests.kt` - Light/dark theme testing
  - `LineIndicatorScreenshotTests.kt` - Simple component
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
