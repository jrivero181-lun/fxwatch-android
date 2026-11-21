# SPEC 01 — Base de inyección de dependencias con Hilt

> **Status:** Implementado
> **Depends on:** Ninguna
> **Date:** 2026-11-21
> **Objective:** Integrar Hilt mediante una convención de Gradle para que una feature nueva solo tenga que aplicar su plugin y declarar `@HiltViewModel`.

## Por qué existe este spec

El dominio ya está terminado, pero sus casos de uso se construyen a mano y no existe un grafo de dependencias.
El plan completo de DI y datos es demasiado grande para un solo cambio.
Este spec cubre solo la base de Hilt, y deja red, persistencia, repositorios y ViewModels para specs posteriores.

## Alcance

**Dentro:**

- Entradas de Hilt, KSP, `javax.inject` y AndroidX Hilt en `gradle/libs.versions.toml`.
- Plugin de convención `fxwatch.android.hilt`.
- Aplicación de ese plugin desde `fxwatch.android.application` y `fxwatch.android.feature`.
- `@Inject constructor` en los cuatro casos de uso de `:core:domain`.
- Eliminación del valor por defecto de `Clock` en `GetRateHistoryUseCase`.
- Qualifiers `@IoDispatcher` y `@DefaultDispatcher` en `:core:domain`.
- Módulo `:core:data` con solo `ClockModule` y `DispatchersModule`.
- `FxWatchApplication` con `@HiltAndroidApp` y `@AndroidEntryPoint` en `MainActivity`.
- `HiltTestRunner` y un smoke test instrumentado del grafo.
- Actualización de `CLAUDE.md` y del skill `di-data-layer-plan`.

**Fuera de alcance (para specs futuros):**

- API, DTOs, mappers y cliente de red.
- DataStore y almacenamiento de favoritos.
- `RatesRepositoryImpl`, `FavoritesRepositoryImpl` y `RepositoryModule`.
- Helper de mapeo de excepciones a `DomainError.Data`.
- ViewModels y pantallas de `:feature:rates` y `:feature:converter`.
- Reemplazo del `Greeting` de `MainActivity`.
- Módulos `@TestInstallIn` con fakes, que se añadirán junto a `RepositoryModule`.

## Modelo de datos

Este spec no introduce modelos de negocio ni datos persistidos.
Introduce los siguientes elementos de DI:

```kotlin
// :core:domain — com.dev.fxwatch.core.domain.di
@Qualifier annotation class IoDispatcher
@Qualifier annotation class DefaultDispatcher

// :core:data — com.dev.fxwatch.core.data.di
// ClockModule: provee Clock (Clock.systemDefaultZone())
// DispatchersModule: provee @IoDispatcher y @DefaultDispatcher
```

Archivos nuevos:

- `build-logic/convention/src/main/kotlin/HiltConventionPlugin.kt`
- `core/domain/src/main/kotlin/com/dev/fxwatch/core/domain/di/DispatcherQualifiers.kt`
- `core/data/build.gradle.kts`
- `core/data/src/main/kotlin/com/dev/fxwatch/core/data/di/ClockModule.kt`
- `core/data/src/main/kotlin/com/dev/fxwatch/core/data/di/DispatchersModule.kt`
- `app/src/main/java/com/dev/fxwatch/FxWatchApplication.kt`
- `app/src/androidTest/java/com/dev/fxwatch/HiltTestRunner.kt`
- `app/src/androidTest/java/com/dev/fxwatch/DiGraphSmokeTest.kt`

Versiones iniciales:

- Hilt `2.60.1`.
- KSP `2.3.14`.
- AndroidX Hilt `1.4.0`, con el artefacto `hilt-lifecycle-viewmodel-compose`.
- `javax.inject` `1`.

## Convención para una feature nueva

1. Crear `feature/<nombre>/build.gradle.kts` con `fxwatch.android.feature` y `android { namespace }`.
2. Añadir `include(":feature:<nombre>")` en `settings.gradle.kts` y la dependencia en `:app`.
3. Anotar el ViewModel con `@HiltViewModel` y `@Inject constructor(casosDeUso...)`.
4. Obtenerlo con `hiltViewModel()` en el composable principal de la pantalla.

Una feature no declara dependencias de Hilt ni crea módulos DI para interfaces de dominio.
Un caso de uso nuevo solo necesita `@Inject constructor`.

## Plan de implementación

1. **Catálogo y classpath.** Añadir versiones, librerías y plugins al catálogo. Añadir los plugins de Hilt y KSP como `implementation` en `build-logic/convention/build.gradle.kts`. Verificar con `./gradlew help`.
2. **Plugin de convención.** Crear `HiltConventionPlugin` y registrarlo como `fxwatch.android.hilt`. Aplica KSP y Hilt, y añade `hilt-android`, `ksp(hilt-compiler)`, `androidTestImplementation(hilt-android-testing)` y `kspAndroidTest(hilt-compiler)`. Todavía ningún módulo lo aplica.
3. **Integración en `:app`.** Aplicar el plugin desde `fxwatch.android.application`. Crear `FxWatchApplication`, declararla en el manifest y anotar `MainActivity`. Verificar con `assembleDebug`. Si falla por incompatibilidad con Kotlin 2.2.10, subir Kotlin a la mínima versión compatible.
4. **Integración en features.** Aplicar el plugin desde `fxwatch.android.feature` y añadir `hilt-lifecycle-viewmodel-compose`. Verificar con `assembleDebug`.
5. **Dominio.** Añadir `javax.inject` como `api` en `:core:domain`. Marcar los cuatro casos de uso con `@Inject constructor`. Quitar el valor por defecto de `Clock`. Crear los qualifiers. Verificar con `:core:domain:test`.
6. **`:core:data`.** Crear el módulo con `fxwatch.android.library` y `fxwatch.android.hilt`, y con `implementation(projects.core.domain)`. Añadir `ClockModule` y `DispatchersModule`, ambos `@InstallIn(SingletonComponent::class)`. Incluirlo en `settings.gradle.kts`. Añadir `:core:data` y `:core:domain` a las dependencias de `:app`.
7. **Smoke test.** Crear `HiltTestRunner`, que arranca `HiltTestApplication`. Configurarlo solo en `app/build.gradle.kts`, sin cambiar `AndroidConfig.TEST_RUNNER`. Crear `DiGraphSmokeTest`, que inyecta `Clock` y ambos dispatchers.
8. **Documentación.** Actualizar el estado del proyecto en `CLAUDE.md`. Marcar en `di-data-layer-plan` lo ya hecho y lo pendiente de `:core:data`, features y `@TestInstallIn`.

## Criterios de aceptación

- [x] `./gradlew ktlintCheck detekt testDebugUnitTest` termina con éxito.
- [x] `./gradlew assembleDebug assembleRelease` termina con éxito.
- [x] Un segundo `./gradlew assembleDebug` reutiliza el configuration cache.
- [x] `./gradlew connectedDebugAndroidTest` pasa en un emulador o dispositivo, incluido `DiGraphSmokeTest`.
- [x] La app debug abre y muestra el `Greeting` sin crash.
- [x] Ningún archivo usa `kapt`.
- [x] Ningún `build.gradle.kts` de módulo declara directamente plugins o librerías de Hilt.
- [x] `:core:domain` no contiene imports de `dagger` ni de `android`.
- [x] `:core:domain` declara `javax.inject` como su única dependencia de DI.
- [x] Los cuatro casos de uso tienen `@Inject constructor`.
- [x] `GetRateHistoryUseCase` no tiene un valor por defecto para `Clock`.
- [x] `:feature:rates` y `:feature:converter` no dependen de `:core:data`.
- [x] `:core:data` contiene únicamente `ClockModule` y `DispatchersModule` como código de producción.

## Decisiones

**Sí: base de Hilt más esqueleto de `:core:data`.**
Los bindings de `Clock` y dispatchers quedan desde el inicio en su módulo definitivo.
El cambio se mantiene pequeño y cualquier fallo se atribuye a Hilt.
Se descartan incluir toda la capa de datos y enlazar temporalmente en `:app`.

**Sí: un único plugin `fxwatch.android.hilt`, aplicado por las convenciones.**
Centraliza KSP, Hilt y sus dependencias en un solo lugar.
Una feature nueva obtiene Hilt solo por aplicar `fxwatch.android.feature`.
Se descarta declarar Hilt en cada módulo porque duplica configuración.

**Sí: qualifiers en `:core:domain`.**
Solo requieren `javax.inject` y ya son visibles para casos de uso, features y datos.
Los bindings siguen en `:core:data`, así que el dominio no conoce la implementación.
Se descartan `:core:data`, invisible para features, y `:core:common`, excesivo para dos anotaciones.

**Sí: `Clock` y dispatchers sin scope.**
Son baratos de obtener y no guardan estado propio.
Sin `@Singleton`, cada inyección nueva toma la zona horaria vigente del sistema.
Se descarta `@Singleton` porque conservaría una zona antigua durante la vida del proceso.

**Sí: quitar el valor por defecto de `Clock`.**
Dagger ignora los argumentos por defecto de Kotlin.
El valor por defecto permitiría crear un reloj inline, contra la regla del proyecto.
Los tests no cambian porque ya pasan un `Clock` explícito.

**Sí: smoke test instrumentado.**
Dagger no valida bindings que nadie solicita, y aún no hay ViewModels.
El test solicita esos bindings y deja lista la base de tests con Hilt.
Se descartan el arranque manual y `fullBindingGraphValidation`.

**Sí: excepción de field injection en `DiGraphSmokeTest`.**
`@HiltAndroidTest` recibe sus dependencias con `@Inject lateinit var`.
Una clase de test no admite constructor injection con el runner de JUnit.
La excepción se limita a tests `@HiltAndroidTest`.

**Sí: subir Kotlin solo si la compilación lo exige.**
Mantiene el cambio mínimo si Kotlin 2.2.10 es compatible.
Si no lo es, se sube a la mínima versión compatible en vez de fijar un Hilt antiguo.
Se descartan congelar Kotlin y actualizarlo de forma preventiva.

## Riesgos

| Riesgo | Mitigación |
| --- | --- |
| Hilt 2.60.1 o KSP 2.3.14 no funcionan con Kotlin 2.2.10 | Subir Kotlin a la mínima versión compatible en el paso 3. |
| Subir Kotlin cambia también el compilador de Compose | Ambos comparten versión en el catálogo. Validar con el comando completo de verificación. |
| Un plugin rompe el configuration cache | El criterio de doble `assembleDebug` lo detecta. |
| R8 elimina clases generadas por Hilt en release | `assembleRelease` forma parte de la aceptación. Añadir reglas en `app/src/main/keepRules/` solo si hacen falta. |
| Un ViewModel pide un caso de uso con repositorio antes del spec de datos | La compilación falla con un binding faltante. Ese trabajo depende del spec de repositorios. |
| `HiltTestRunner` afecta a `ExampleInstrumentedTest` | `connectedDebugAndroidTest` ejecuta ambos tests. |

## Lo que **no** está en este spec

- Red, DTOs y mappers.
- DataStore y favoritos.
- Implementaciones de repositorios y `RepositoryModule`.
- ViewModels y pantallas.
- Fakes con `@TestInstallIn`.

Cada uno de estos puntos irá en su propio spec.
