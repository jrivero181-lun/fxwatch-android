import com.dev.fxwatch.buildlogic.AndroidConfig
import com.dev.fxwatch.buildlogic.addUnitTestDependencies
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Pure Kotlin/JVM module (no Android). Used by :core:domain and :core:testing. */
class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-jvm"))
            pluginManager.apply(libs.pluginId("fxwatch-quality"))

            extensions.configure<JavaPluginExtension> {
                sourceCompatibility = AndroidConfig.JAVA_VERSION
                targetCompatibility = AndroidConfig.JAVA_VERSION
            }
            extensions.configure<KotlinJvmProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(AndroidConfig.JAVA_VERSION.toString()))
                }
            }
            addUnitTestDependencies()

            // JVM modules only have `test`. This alias makes the repo-wide
            // `./gradlew testDebugUnitTest` also run JVM tests instead of skipping them.
            tasks.register("testDebugUnitTest") {
                group = "verification"
                description = "Alias of `test` so the repo-wide unit test command covers JVM modules."
                dependsOn("test")
            }
        }
    }
}
