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
- JDK 17
- `secret.properties` file in project root with `MAPS_API_KEY`
- `app/google-services.json` for Firebase services
- `version.properties` file in project root (managed by release scripts)

### Build Configuration
- Compile SDK: 36, Target SDK: 36, Min SDK: 26
- Kotlin with JVM target 17
- ProGuard enabled for release builds with optimization
- Signing configs: Release (uses `certs/release.keystore`) and Debug (uses `certs/debug.keystore`)
- KSP for Room code generation
- Room schemas stored in `app/schemas/`
- Version management via `version.properties` (major.minor.patch format)

### Testing
- Unit tests: JUnit, Strikt assertions, Mockito-Kotlin, Coroutines testing
- Instrumentation tests available via `connectedAndroidTest`
- Debug builds include:
  - Chucker for network debugging
  - Debug menu module (`:debug-menu`) for development tools
  - No-op implementation for release builds

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
