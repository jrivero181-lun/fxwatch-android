import com.dev.fxwatch.buildlogic.AndroidConfig
import com.dev.fxwatch.buildlogic.lib
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitivity
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.getByType

/**
 * Static analysis: ktlint (rules from .editorconfig) + detekt (config/detekt/detekt.yml).
 *
 * detekt runs as `detekt-cli` in a forked JVM on a JDK 17 toolchain: the stable detekt 1.23.x
 * embeds a Kotlin compiler that can't parse the Gradle daemon's JDK 25 version string, so it
 * can't run in-process through its Gradle plugin.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("ktlint"))
            registerDetekt()
        }
    }

    private fun Project.registerDetekt() {
        val detektCli = configurations.create("detektCli") {
            isCanBeConsumed = false
            isVisible = false
        }
        dependencies.add(detektCli.name, libs.lib("detekt-cli"))

        val sourceDirs = SOURCE_SETS
            .flatMap { set -> listOf("src/$set/java", "src/$set/kotlin") }
            .map(::file)
            .filter { it.isDirectory }
        val configFile = rootProject.layout.projectDirectory.file("config/detekt/detekt.yml")
        val reportsDir = layout.buildDirectory.dir("reports/detekt")
        val toolchains = extensions.getByType<JavaToolchainService>()

        val detekt = tasks.register("detekt", JavaExec::class.java) {
            group = "verification"
            description = "Runs detekt static analysis on this module's Kotlin sources."
            classpath = detektCli
            mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
            javaLauncher.set(
                toolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(DETEKT_JDK)) },
            )

            inputs.files(sourceDirs).withPathSensitivity(PathSensitivity.RELATIVE)
            inputs.file(configFile).withPathSensitivity(PathSensitivity.NONE)
            outputs.dir(reportsDir)
            onlyIf("module has Kotlin sources") { sourceDirs.isNotEmpty() }

            val reports = reportsDir.get().asFile
            args(
                "--input", sourceDirs.joinToString(",") { it.absolutePath },
                "--config", configFile.asFile.absolutePath,
                "--build-upon-default-config",
                "--jvm-target", AndroidConfig.JAVA_VERSION.toString(),
                "--base-path", projectDir.absolutePath,
                "--report", "txt:${reports.resolve("detekt.txt")}",
                "--report", "html:${reports.resolve("detekt.html")}",
                "--report", "sarif:${reports.resolve("detekt.sarif")}",
            )
        }
        pluginManager.withPlugin("base") {
            tasks.named("check") { dependsOn(detekt) }
        }
    }

    private companion object {
        const val DETEKT_JDK = 17
        val SOURCE_SETS = listOf("main", "test", "androidTest", "debug", "release", "testFixtures")
    }
}
