plugins {
    alias(libs.plugins.fxwatch.android.library)
    alias(libs.plugins.fxwatch.android.hilt)
}

android {
    namespace = "com.dev.fxwatch.core.data"
}

dependencies {
    implementation(projects.core.domain)
}
