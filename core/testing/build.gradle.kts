plugins {
    alias(libs.plugins.fxwatch.jvm.library)
}

dependencies {
    api(projects.core.domain)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
