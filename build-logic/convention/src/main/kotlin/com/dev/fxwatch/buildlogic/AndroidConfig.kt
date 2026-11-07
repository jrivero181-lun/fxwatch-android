package com.dev.fxwatch.buildlogic

import org.gradle.api.JavaVersion

internal object AndroidConfig {
    const val COMPILE_SDK = 37
    const val TARGET_SDK = 37
    const val MIN_SDK = 26
    const val TEST_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_11
}
