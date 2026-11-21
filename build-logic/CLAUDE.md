# build-logic

- **detekt is not the Gradle plugin.** `fxwatch.quality` registers a `detekt` `JavaExec` task that runs `detekt-cli` on a JDK 17 toolchain, because detekt 1.23.x can't run in-process on the JDK 25 daemon. Config overrides: `config/detekt/detekt.yml` (includes `MagicNumber.ignoreEnums: true`, so enum entries like `HistoryPeriod.WEEK(7)` don't need constants).
- **JVM modules get a `testDebugUnitTest` alias** for `test`, so the repo-wide `testDebugUnitTest` command also runs their tests.
