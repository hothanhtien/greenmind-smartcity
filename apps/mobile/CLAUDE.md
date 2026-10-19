# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug    # Build debug APK
./gradlew :composeApp:build            # Full build with tests
./gradlew :composeApp:clean            # Clean build artifacts
./gradlew :composeApp:test             # Run unit tests

# iOS: Open iosApp/ in Xcode and run from there
```

## Project Overview

GreenMind is a **Kotlin Multiplatform (KMP)** app targeting **Android and iOS** using **Compose Multiplatform** for shared UI. The single module is `composeApp`.

**Target platforms:** Android (API 24+), iOS (arm64 + simulator arm64)
**Backend:** REST API at `https://vodang-api.gauas.com`

## Architecture

### Navigation
`App.kt` uses a simple `AppScreen` enum (`LOGIN`, `REGISTER`, `HOME`) with mutable state. Navigation is driven by `SettingsStore`: if an access token exists, the app goes directly to `HOME`.

### State Management
`store/SettingsStore.kt` is a singleton `object` using `multiplatform-settings` for persistent key-value storage. It exposes `StateFlow<T>` for reactive updates and holds `accessToken`, `refreshToken`, and user data.

### API Layer
`api/ApiClient.kt` configures a Ktor HTTP client with content negotiation (JSON) and logging. All API calls use `kotlinx.serialization`. Platform-specific engines: `ktor-client-android` / `ktor-client-darwin`.

**Auth endpoints:**
- `POST /auth/login/email` → `LoginEmailRequest` / `LoginEmailResponse`
- `POST /auth/register/email` → `RegisterEmailRequest` / `RegisterEmailResponse`

### Platform-Specific Code
Platform differences are handled via Kotlin `expect`/`actual` declarations:
- `location/GeolocationService.kt` — expect class; Android uses `LocationForegroundService`
- `camera/CameraService.kt` — expect class; Android uses CameraX
- `permission/PermissionRequester.kt` — expect class; Android uses `ActivityResultContracts`
- `accounts/AccountsRepository.kt` — expect class for multi-account storage

Android entry point: `MainActivity.kt` initializes camera, location, and permissions before calling `App()`.

## Key Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| Compose Multiplatform | 1.10.0 | Shared UI |
| Ktor Client | 3.1.3 | HTTP networking |
| kotlinx.serialization | 1.8.0 | JSON parsing |
| multiplatform-settings | 1.1.1 | Persistent KV storage |
| KoalaPlot | 0.11.0 | Charts (electricity card) |
| CameraX | 1.4.1 | Android camera |
| Play Services Location | 21.0.1 | Android GPS |

## Feature Catalogue

`catalogue/CatalogueScreen.kt` is the **single source of truth** for all app features. It is accessible from the drawer (left menu) by any user.

**Rule: whenever a new feature is added, changed, or removed, you MUST update `CatalogueScreen.kt`** — specifically the feature lists inside `CatalogueScreen()`. Add a new `FeatureEntry` to the appropriate role section (`allFeatures`, `householdFeatures`, `collectorFeatures`, `volunteerFeatures`).

Current features tracked:
- **All users**: Todos, Surveys, OCEAN Score
- **Household**: Waste Sorting, Garbage Drop, Electricity Usage, Scan Meal, Scan Bill, Walk Distance
- **Collector**: Waste Heatmap, Collection Route, Check-in Collection
- **Volunteer**: Volunteer Events

## Source Layout

```
composeApp/src/
├── commonMain/kotlin/com/vodang/greenmind/
│   ├── App.kt                    # Entry point, navigation
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   ├── api/                      # ApiClient + AuthApi (data models, calls)
│   ├── store/                    # SettingsStore (tokens, user)
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── components/           # Reusable home UI cards/components
│   ├── location/                 # GeolocationService (expect + data)
│   ├── camera/                   # CameraService (expect)
│   ├── permission/               # PermissionRequester (expect)
│   └── theme/                    # Theme constants (colors, shapes)
├── androidMain/                  # Android actuals + MainActivity
└── iosMain/                      # iOS actuals
```

## Code Standards

### Naming Conventions
- **Colors**: PascalCase (e.g., `Gray700`, `Green50`) — use theme constants from `theme/GreenMindColors.kt`
- **Shapes**: Use `GreenMindShapes` from `theme/GreenMindShapes.kt`
- **Functions**: camelCase, verb-first (e.g., `fetchData`, not `getData`)
- **Screens**: `<Feature>Screen.kt` (e.g., `BlogScreen`, not `BlogListScreen`)
- **Components**: `<Feature><Type>.kt` (e.g., `BlogListItem`, `BlogDetailHeader`)
- **Avoid suffix "s" in color names** (e.g., `gray700s` → `gray700`)

### Scaffold Requirement
**ALL screens must use AppScaffold or Scaffold with TopAppBar.**
- Wrap main content in `AppScaffold(title = ..., subtitle = ...)` from `components/AppScaffold.kt`
- No raw `Box`/`Column` for full-screen layouts
- Exception: Router screens that only delegate to child screens

### File Size Limits
- Target: <200 lines per file
- Maximum: 300 lines (flag for review if exceeded)
- If file exceeds 300 lines, extract components into separate files

### Component Organization
- **Shared UI**: `components/` (buttons, cards, dialogs, scaffold)
- **Feature UI**: `feature/components/` (feature-specific components)
- **Screens**: root level or `feature/Screen.kt`
- **Never name a component as `*Screen.kt`** if it's not a screen — use `*Card.kt`, `*List.kt`, `*Dialog.kt`

### Theme Usage
Use theme constants instead of hardcoded colors:
```kotlin
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.SurfaceGray
import com.vodang.greenmind.theme.GreenMindShapes
```
