---
name: di-data-layer-plan
description: Ordered plan for adding Hilt dependency injection, the :core:data module, feature ViewModels and :app wiring in FxWatch. Use before working on any of those steps.
---

# Dependency injection and data layer plan

Spec 01 implemented the Hilt foundation. Each step below is marked **Done**, **Partial** or **Pending**. Follow this order for what remains. Each step must leave `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` green.

1. **Done: version catalog.** Pinned to Hilt 2.60.1, KSP 2.3.14 and AndroidX Hilt 1.4.0. Add Hilt (2.59.2 or later), KSP (2.3.1 or later, the first release that supports AGP 9's built-in Kotlin), `javax.inject`, the AndroidX Hilt Compose artifact (`hiltViewModel()`) and `hilt-android-testing`. Also add the Hilt and KSP Gradle plugins as `implementation` dependencies of `build-logic`. Check the latest compatible versions before pinning them.
2. **Done: build-logic.** Add a `HiltConventionPlugin` (`fxwatch.android.hilt`) that applies `com.google.devtools.ksp` and `com.google.dagger.hilt.android`, and adds `hilt-android` plus `ksp(hilt-compiler)`. `fxwatch.android.feature` and `fxwatch.android.application` apply it. `fxwatch.jvm.library` does not.
3. **Done: `:core:domain`.** Add `javax.inject` and mark every use case `@Inject constructor`, so a new use case never needs a provider. `GetRateHistoryUseCase` keeps its `Clock` parameter with no default value, because Dagger ignores Kotlin default arguments. The dispatcher qualifiers live in `core.domain.di`.
4. **Partial: module `:core:data`** (Android library + `fxwatch.android.hilt`, depends on `:core:domain`). The module, `DispatchersModule` and `ClockModule` are done. Everything else listed here is pending:
   - `remote/`: API service and DTOs (`rates`, `currencies`, rate history), plus mappers to domain models. The DTOs never leave this module.
   - `local/`: favorites storage (DataStore).
   - `repository/`: `RatesRepositoryImpl` and `FavoritesRepositoryImpl`, `@Singleton` and `@Inject constructor`.
   - `error/`: the helper that maps exceptions to `DomainError.Data` (`IOException` → `Network`, HTTP codes → `Server`, unknown currency → `UnsupportedCurrency`). It must rethrow `CancellationException`.
   - `di/`: `RepositoryModule` (`@Binds` interface → impl), `NetworkModule` and `StorageModule`, next to the existing `DispatchersModule` and `ClockModule`.
   - Tests: mappers and the exception helper with JUnit; repository implementations with fake data sources and an injected test dispatcher.
5. **Pending: features.** The Hilt convention is already applied, so only the code is missing. ViewModels are `@HiltViewModel` + `@Inject constructor(useCases…)`. `:feature:rates` (MVVM + UDF) and `:feature:converter` (MVI) depend only on `:core:domain`, never on `:core:data`. Their tests build the ViewModel by hand with fakes and `MainDispatcherRule`.
6. **Partial: `:app`.** Done: `FxWatchApplication` with `@HiltAndroidApp`, `@AndroidEntryPoint` on `MainActivity`, `HiltTestRunner`, `DiGraphSmokeTest` and the dependencies on `:core:data` and the features. `:app` is the only module that sees both, so it's where the graph is assembled. Pending: instrumented tests replace `RepositoryModule` with `@TestInstallIn` modules that bind the fakes.
