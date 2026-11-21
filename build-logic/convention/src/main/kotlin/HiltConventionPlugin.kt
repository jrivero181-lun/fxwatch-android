import com.dev.fxwatch.buildlogic.lib
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Hilt with KSP (never kapt) for Android modules, including instrumented test support. */
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("ksp"))
            pluginManager.apply(libs.pluginId("hilt"))

            with(dependencies) {
                add("implementation", libs.lib("hilt-android"))
                add("ksp", libs.lib("hilt-compiler"))
                add("androidTestImplementation", libs.lib("hilt-android-testing"))
                add("kspAndroidTest", libs.lib("hilt-compiler"))
            }
        }
    }
}
