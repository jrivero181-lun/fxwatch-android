import com.android.build.api.dsl.LibraryExtension
import com.dev.fxwatch.buildlogic.AndroidConfig
import com.dev.fxwatch.buildlogic.addUnitTestDependencies
import com.dev.fxwatch.buildlogic.libs
import com.dev.fxwatch.buildlogic.pluginId
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("android-library"))
            pluginManager.apply(libs.pluginId("fxwatch-quality"))

            extensions.configure<LibraryExtension> {
                compileSdk {
                    version = release(AndroidConfig.COMPILE_SDK)
                }
                defaultConfig {
                    minSdk = AndroidConfig.MIN_SDK
                    testInstrumentationRunner = AndroidConfig.TEST_RUNNER
                }
                compileOptions {
                    sourceCompatibility = AndroidConfig.JAVA_VERSION
                    targetCompatibility = AndroidConfig.JAVA_VERSION
                }
            }
            addUnitTestDependencies()
        }
    }
}
