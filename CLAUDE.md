# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

The domain layer (`:core:domain`) and its test support (`:core:testing`) are done. There is no data layer yet, and the feature modules are still empty. `:app` still holds the template `MainActivity` (`Greeting`) and the Material 3 theme in `ui/theme/`; treat that as a placeholder to replace, not a pattern to follow.

Modules:
- `:core:domain`, `:core:testing`: pure Kotlin/JVM (`fxwatch.jvm.library`). No Android allowed.
- `:feature:rates`, `:feature:converter`: Android library + Compose (`fxwatch.android.feature`), which already depends on `:core:domain` and, for tests, `:core:testing`. Features don't depend on each other; `:app` integrates them. Both empty.

When adding features, put code in the corresponding module, not in `:app`. A new module only needs a `build.gradle.kts` applying a convention plugin (plus `android { namespace }` for Android modules) and an `include` in `settings.gradle.kts`.

### :core:domain (`com.dev.fxwatch.core.domain`)

- `model/`: `Currency` (+ `CurrencyType`: `FIAT`, `CRYPTO`, `OTHER`), `ExchangeRate` (`1 base = rate quote` on a date), `RateHistory`/`RatePoint`, `HistoryPeriod` (`WEEK`, `MONTH`, `QUARTER`, `YEAR`, in days). These mirror the API objects (`rates`, `currencies`, rate history) but are not DTOs; parsing belongs to the data layer.
- `repository/`: `RatesRepository` (currencies, latest rates for a base, history for a pair between two inclusive dates) and `FavoritesRepository` (favorite currency **codes** as `Flow<Set<String>>`, plus add/remove).
- `usecase/`: `GetLatestRatesUseCase`, `ConvertAmountUseCase`, `ToggleFavoriteUseCase`, `GetRateHistoryUseCase`. One class per action, with a single `operator fun invoke`.
- `result/`: `DomainResult<T>` (`Success(data)` / `Failure(error)`) with extensions (`map`, `flatMap`, `fold`, `onSuccess`, `onFailure`, `getOrNull`, `errorOrNull`, `asSuccess`, `asFailure`), and the sealed `DomainError`, split into `Data` (`Network`, `Server(code)`, `Storage`, `UnsupportedCurrency`, `Unknown(cause)`) and `Validation` (`NegativeAmount`, `SameCurrency`, `RateUnavailable`).

Decisions to keep:
- **Errors:** use `DomainResult`, not `kotlin.Result`, which only carries an untyped `Throwable`. Repositories return `DomainResult` and must not throw for expected failures. The data layer is the only place that maps exceptions (`IOException`, HTTP codes) to `DomainError.Data`. Don't use `runCatching` for that, since it swallows `CancellationException`; rethrow it. Programming errors (e.g. rates with mixed bases) still throw (`require`).
- **Adding a `DomainError` breaks every exhaustive `when` on it.** This is intended: the compiler then points to every place that must handle the new case.
- **Money is `BigDecimal`, dates are `java.time.LocalDate`.** `ConvertAmountUseCase` divides with `MathContext.DECIMAL64` (16 significant digits, `HALF_EVEN`). Rounding for display (2 decimals, or each currency's own) belongs to presentation, not the domain.
- **`ConvertAmountUseCase` is pure and synchronous.** It takes the rates as a parameter, so the MVI converter loads them once and recomputes on every keystroke. Neither currency has to be the base: it goes through a cross rate, with the base counting as rate 1.
- **"Today" is injected:** `GetRateHistoryUseCase` takes a `java.time.Clock` (system default in production).
- `kotlinx-coroutines-core` is `api` in `:core:domain`, because `Flow` is part of its public API.

### :core:testing (`com.dev.fxwatch.core.testing`)

- `FakeRatesRepository`: seed it through `currencies`, `latestRates` (keyed by base) and `history` (filtered by the requested inclusive range and returned for the requested pair). Set `error: DomainError?` to make every call fail. `lastHistoryRequest` records the last history call, even when it fails.
- `FakeFavoritesRepository`: backed by a `StateFlow`, so a Turbine subscriber first receives the current set. Set `error` to make writes fail without changing the favorites.
- `MainDispatcherRule`: JUnit 4 rule (`@get:Rule`) for any test that touches `viewModelScope`. It defaults to `UnconfinedTestDispatcher`; pass a `StandardTestDispatcher` to control execution.
- The fakes have their own tests (`FakeRatesRepositoryTest`), because other tests rely on them honoring the repository contracts. Update those tests when a fake changes.

Pending for the data layer: a helper that maps exceptions to `DomainError` and rethrows `CancellationException`. It wasn't added yet because nothing uses it.

### build-logic

`build-logic/` is an included build with the convention plugins (`build-logic/convention/src/main/kotlin/`). SDK levels and the Java target are in `AndroidConfig.kt`. Plugins are on the classpath through build-logic's `implementation` dependencies, so the root `build.gradle.kts` has no `apply false` entries; don't add them. Modules reference each other with type-safe accessors (`projects.core.domain`).

- **detekt is not the Gradle plugin.** `fxwatch.quality` registers a `detekt` `JavaExec` task that runs `detekt-cli` on a JDK 17 toolchain, because detekt 1.23.x can't run in-process on the JDK 25 daemon. Config overrides: `config/detekt/detekt.yml` (includes `MagicNumber.ignoreEnums: true`, so enum entries like `HistoryPeriod.WEEK(7)` don't need constants).
- **JVM modules get a `testDebugUnitTest` alias** for `test`, so the repo-wide command below also runs their tests.
- ktlint rules come from `.editorconfig` (`android_studio` style). Fix formatting with `./gradlew ktlintFormat`.

CI (`.github/workflows/ci.yml`) runs `ktlintCheck detekt testDebugUnitTest`, then `lintDebug assembleDebug`, on every PR and on pushes to `master`.

## Commands

Run from the repo root. On Windows PowerShell use `.\gradlew.bat`; in Git Bash use `./gradlew`.

```
./gradlew assembleDebug                 # build debug APK
./gradlew installDebug                  # install on connected device/emulator
./gradlew assembleRelease               # release build (R8 optimization enabled)
./gradlew testDebugUnitTest             # unit tests in every module (Android and JVM)
./gradlew ktlintCheck detekt            # static analysis (ktlintFormat to auto-fix)
./gradlew connectedDebugAndroidTest     # instrumented tests (app/src/androidTest, needs a device)
./gradlew lint                          # Android lint

# single unit test class / method
./gradlew testDebugUnitTest --tests "com.dev.fxwatch.ExampleUnitTest"
./gradlew testDebugUnitTest --tests "com.dev.fxwatch.ExampleUnitTest.addition_isCorrect"

# single instrumented test class
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dev.fxwatch.ExampleInstrumentedTest
```

## Build setup

These are non-obvious because the project uses newer tooling than most examples online:

- **AGP 9.x with built-in Kotlin.** There is no `org.jetbrains.kotlin.android` plugin: Android modules apply only the AGP plugin (plus `org.jetbrains.kotlin.plugin.compose`). Don't add the kotlin-android plugin or a `kotlinOptions` block. Only the pure-JVM modules apply `org.jetbrains.kotlin.jvm`.
- **The release minification DSL is `optimization { enable = true }`**, not `isMinifyEnabled`/`proguardFiles`. R8 keep rules live in `app/src/main/keepRules/*.keep`, and AGP picks up every file in that directory. There is no `proguard-rules.pro`.
- **`compileSdk { version = release(37) }`**: new-style DSL. targetSdk 37, minSdk 26, Java/bytecode target 17 (set in `build-logic` `AndroidConfig.kt`).
- **Dependencies go through the version catalog** at `gradle/libs.versions.toml`. Compose artifacts are versioned by the Compose BOM, so leave them without explicit versions. `settings.gradle.kts` uses `FAIL_ON_PROJECT_REPOS`, so declare repositories only there, not in module build files.
- **The Gradle configuration cache is enabled** (`gradle.properties`). Build logic must stay configuration-cache compatible.
- **The Gradle daemon runs on a JDK 25 toolchain** auto-provisioned through foojay (`gradle/gradle-daemon-jvm.properties`). Gradle wrapper is 9.6.0.

## Code layout

- Package/namespace: `com.dev.fxwatch`
- Compose theme entry point: `FxWatchTheme` (`ui/theme/Theme.kt`). It uses dynamic color on Android 12+ and falls back to the static schemes in `Color.kt`.
- `local.properties` (SDK path) is machine-specific and git-ignored.

## Project rules

**Stack:** Kotlin + Jetpack Compose.

**Architecture (Clean Architecture)**
- `:core:domain` must not depend on Android.
- `:feature:rates` uses MVVM + UDF.
- `:feature:converter` uses MVI.

**Testing**
- Every new domain class or ViewModel must include tests (JUnit, MockK, Turbine, `runTest`).
- Use fakes in `:core:testing` instead of mocks whenever possible.

**Dependencies**
- Manage dependencies exclusively via `gradle/libs.versions.toml`.
- Manage plugins via `build-logic`.

**Commits:** Follow Conventional Commits (`feat:`, `fix:`, `test:`, `refactor:`, `docs:`, `chore:`).

**Before finishing**, run:

```
./gradlew ktlintCheck detekt testDebugUnitTest
```

**Architectural decisions:** Provide a 3-line explanation for every architectural decision you make.
