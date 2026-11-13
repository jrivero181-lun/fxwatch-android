plugins {
    alias(libs.plugins.fxwatch.jvm.library)
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
