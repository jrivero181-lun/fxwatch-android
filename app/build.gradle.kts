plugins {
    alias(libs.plugins.fxwatch.android.application)
    alias(libs.plugins.fxwatch.android.compose)
}

android {
    namespace = "com.dev.fxwatch"

    defaultConfig {
        applicationId = "com.dev.fxwatch"
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
}

dependencies {
    implementation(projects.feature.rates)
    implementation(projects.feature.converter)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
