package com.dev.fxwatch.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).get().get().pluginId

/** Unit-test stack shared by every module: JUnit4, MockK, Turbine and coroutines-test (runTest). */
internal fun Project.addUnitTestDependencies() {
    listOf("junit", "mockk", "turbine", "kotlinx-coroutines-test").forEach { alias ->
        dependencies.add("testImplementation", libs.lib(alias))
    }
}
