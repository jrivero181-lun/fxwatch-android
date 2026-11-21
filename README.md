# FxWatch

[![CI](https://github.com/jrivero181-lun/fxwatch-android/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/jrivero181-lun/fxwatch-android/actions/workflows/ci.yml)

An Android app for tracking exchange rates and converting currencies, built with Kotlin, Jetpack Compose and a modular Clean Architecture.

> **Status: in development.** The domain layer, test support, Hilt foundation and CI are in place. Repositories, ViewModels and screens are next.

## Roadmap

- [x] Domain models, repository contracts and use cases
- [x] Shared test fakes
- [x] Hilt foundation with KSP
- [x] CI with static analysis, unit tests and debug build
- [ ] Network, local storage and repositories
- [ ] Rates screen (MVVM + UDF)
- [ ] Converter screen (MVI)

## Tech stack

- **UI:** Jetpack Compose, Material 3
- **Async:** Kotlin Coroutines, Flow
- **DI:** Hilt with KSP
- **Testing:** JUnit, MockK, Turbine, hand-written fakes
- **Quality:** ktlint, detekt, Android Lint, GitHub Actions
- **Build:** AGP 9 with built-in Kotlin, convention plugins, version catalog, configuration cache

## Architecture

```text
:app → :feature:* → :core:domain ← :core:data ← :app
```

| Module | Responsibility | Status |
| --- | --- | --- |
| `:app` | Integrates features and data, assembles the DI graph | Placeholder UI |
| `:feature:rates` | Exchange rates screen (MVVM + UDF) | Planned |
| `:feature:converter` | Currency converter screen (MVI) | Planned |
| `:core:domain` | Models, repository contracts and use cases in pure Kotlin | Done |
| `:core:data` | Repository implementations and data bindings | Clock and dispatchers only |
| `:core:testing` | Shared fakes and test utilities | Done |
| `build-logic` | Gradle convention plugins | Done |

### Key decisions

- **Framework-free domain:** `:core:domain` has no Android or Hilt dependency, so it is tested on the JVM.
- **Typed errors:** repositories return `DomainResult` instead of throwing for expected failures.
- **Precise money:** amounts use `BigDecimal`, and display rounding belongs to presentation.
- **Pure conversion:** rates are loaded once and the converter can recompute on every keystroke.
- **Deterministic tests:** `Clock` and dispatchers are injected, and unit tests use fakes without Hilt.

## Getting started

Requires JDK 17+ and Android SDK 37. Gradle provisions its JDK 25 daemon automatically. The app targets Android 8.0 (API 26) and later.

```bash
git clone https://github.com/jrivero181-lun/fxwatch-android.git
cd fxwatch-android
./gradlew assembleDebug
```

Run the quality checks and unit tests:

```bash
./gradlew ktlintCheck detekt testDebugUnitTest
```

On Windows PowerShell, use `.\gradlew.bat`.
