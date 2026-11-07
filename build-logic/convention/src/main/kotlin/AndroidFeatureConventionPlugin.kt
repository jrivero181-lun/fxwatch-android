import com.dev.fxwatch.buildlogic.lib
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Feature module: Android library + Compose + ViewModel, wired to :core:domain and :core:testing. */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("fxwatch-android-library"))
            pluginManager.apply(libs.pluginId("fxwatch-android-compose"))

            with(dependencies) {
                add("implementation", project(":core:domain"))
                add("implementation", libs.lib("kotlinx-coroutines-core"))
                add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
                add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
                add("testImplementation", project(":core:testing"))
            }
        }
    }
}
