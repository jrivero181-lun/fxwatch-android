package com.dev.fxwatch.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit 4 rule that replaces [Dispatchers.Main] with [testDispatcher] for the duration of each test
 * and resets it afterward, even if the test fails. Needed by any test that touches `viewModelScope`.
 *
 * Defaults to [UnconfinedTestDispatcher] so coroutines launched on Main run eagerly; pass a
 * `StandardTestDispatcher` to control execution with its scheduler. `runTest` shares the scheduler
 * of the Main test dispatcher, so virtual time stays consistent between both.
 *
 * ```
 * @get:Rule val mainDispatcherRule = MainDispatcherRule()
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
