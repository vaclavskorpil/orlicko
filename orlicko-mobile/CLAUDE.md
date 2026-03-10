# Orlicko - Radio Orlicko KMP App

## Project Overview

Kotlin Multiplatform (KMP) + Compose Multiplatform radio streaming app targeting Android and iOS.
- **Kotlin 2.3.0**, **Compose Multiplatform 1.10.0**
- Package: `cz.skorpil.orlicko`
- Single module: `composeApp`

## Project Structure

```
composeApp/src/
  commonMain/kotlin/cz/skorpil/orlicko/
    App.kt                    # Root composable
    Platform.kt               # Platform interface (expect/actual)
    data/                     # Services (MetadataService - Ktor HTTP)
    player/                   # Audio player abstraction (expect/actual RadioPlayer)
    ui/                       # Compose screens and components
    viewmodel/                # ViewModels with StateFlow
  androidMain/                # Android: ExoPlayer, MainActivity, AndroidContext
  iosMain/                    # iOS: AVPlayer, MainViewController
```

## KMP Best Practices

### Architecture

- **All business logic belongs in `commonMain`.** Only put truly platform-specific code (audio engines, system APIs) in platform source sets.
- Use **MVVM** with `androidx.lifecycle.ViewModel` in `commonMain` (already multiplatform).
- Use `StateFlow` + `collectAsState()` for unidirectional reactive state.
- On non-JVM platforms, provide a factory lambda: `viewModel { MyViewModel() }` (no reflection).

### Expect/Actual

- Use **sparingly** -- only for small, clearly platform-divergent pieces (player engines, platform identifiers).
- Prefer **interfaces + DI** for complex abstractions that benefit from testability.
- Use expect/actual at the DI boundary (e.g., `expect val platformModule`) and inject everything else via interfaces.

### Dependency Injection

- **Koin** is the recommended DI for KMP -- simplest setup, full CMP support with `koinViewModel()`.
- Define modules in `commonMain`, use `expect val platformModule` for platform-specific bindings.
- Hilt is NOT available for KMP (Android-only).

### Networking (Ktor)

- Engines: **OkHttp** for Android, **Darwin** for iOS (already configured correctly).
- Define `HttpClient` configuration in `commonMain` (content negotiation, serialization plugins).
- Use expect/actual or DI to provide platform-specific engine.
- Add `ktor-client-content-negotiation` + `ktor-serialization-kotlinx-json` for JSON handling.

### Navigation

- **Navigation 3** (available since CMP 1.10.0) is the forward-looking choice:
  - User-owned back stack (`SnapshotStateList`), type-safe `@Serializable` routes implementing `NavKey`.
  - On non-JVM: use `SavedStateConfiguration` with `SerializersModule` for route serialization.
- Navigation 2 (`compose.navigation`) still works but Navigation 3 is recommended for new code.

### Resources

- Use `compose.components.resources` (official library, already configured).
- Place resources in `composeResources/` with subdirs: `drawable/`, `font/`, `values/`, `files/`.
- Qualifiers via hyphens: `drawable-dark/`, `values-cs/`, `drawable-xxhdpi/`.
- Access via generated `Res` object: `stringResource(Res.string.x)`, `painterResource(Res.drawable.x)`.

### Coroutines

- `Dispatchers.Main` works on Android and iOS out of the box.
- For desktop JVM: add `kotlinx-coroutines-swing` to make `Dispatchers.Main` available.
- `Dispatchers.IO` is available on Kotlin/Native (since coroutines ~1.7) but without elasticity.
- **Never swallow `CancellationException`** -- always rethrow it or guard with `if (e is CancellationException) throw e`.
- Prefer injecting dispatchers for testability.

### Testing

- Write tests in `commonTest` first -- they run on all targets automatically.
- Prefer **fakes over mocks** (work across all platforms without codegen).
- Use **Turbine** for testing Flows, `kotlinx-coroutines-test` (`runTest`, `TestDispatcher`) for async.
- Use Ktor `MockEngine` for network layer testing.
- Tag Compose UI elements with `testTag` for stable test selectors.

### Common Pitfalls to Avoid

- **Swallowing `CancellationException`** in general `catch (e: Exception)` blocks breaks cooperative cancellation.
- **Logic forking** -- duplicating logic in platform source sets instead of keeping it in `commonMain`.
- **Expect/actual overuse** -- use interfaces + DI for complex abstractions.
- **iOS single-framework limit** -- only one KMP framework per iOS app; use umbrella module for multi-module projects.
- **Desktop `Dispatchers.Main`** -- forgetting `kotlinx-coroutines-swing` causes crashes.

### Gradle

- Use version catalog (`libs.versions.toml`) for all versions -- no hardcoded versions in build scripts.
- Don't declare `kotlin-stdlib` explicitly (Kotlin Gradle Plugin adds it automatically).
- Enable configuration cache: `org.gradle.configuration-cache=true` in `gradle.properties`.
- Use `linkDebug*` tasks during development instead of full `build` to avoid compiling all targets.
- Prefer KSP over kapt for annotation processors (faster, KMP-compatible).
