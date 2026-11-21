plugins {
    alias(libs.plugins.fxwatch.jvm.library)
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)

    testImplementation(projects.core.testing)
}
