import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.dev.fxwatch.buildlogic.lib
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

/** Enables Compose on an Android application or library module. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-compose"))

            pluginManager.withPlugin(libs.pluginId("android-application")) {
                extensions.getByType<ApplicationExtension>().buildFeatures.compose = true
            }
            pluginManager.withPlugin(libs.pluginId("android-library")) {
                extensions.getByType<LibraryExtension>().buildFeatures.compose = true
            }

            with(dependencies) {
                val bom = platform(libs.lib("androidx-compose-bom"))
                add("implementation", bom)
                add("implementation", libs.lib("androidx-compose-ui"))
                add("implementation", libs.lib("androidx-compose-ui-graphics"))
                add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
                add("implementation", libs.lib("androidx-compose-material3"))
                add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
                add("androidTestImplementation", bom)
            }
        }
    }
}
