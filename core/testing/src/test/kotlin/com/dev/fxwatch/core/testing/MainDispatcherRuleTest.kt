package com.dev.fxwatch.core.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runners.model.Statement

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRuleTest {

    /** Runs [body] the way JUnit would when the rule is declared with `@get:Rule`. */
    private fun MainDispatcherRule.run(body: () -> Unit) {
        apply(
            object : Statement() {
                override fun evaluate() = body()
            },
            Description.EMPTY
        ).evaluate()
    }

    /** Launches [block] on Main in a throwaway scope, so nothing outlives the test. */
    private fun launchOnMain(block: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch { block() }
        scope.cancel()
    }

    @Test
    fun `default dispatcher runs Main coroutines eagerly`() {
        MainDispatcherRule().run {
            var ran = false

            launchOnMain { ran = true }

            assertTrue(ran)
        }
    }

    @Test
    fun `a StandardTestDispatcher defers Main coroutines until its scheduler runs`() {
        val dispatcher = StandardTestDispatcher()
        val rule = MainDispatcherRule(dispatcher)

        rule.run {
            var ran = false
            val scope = CoroutineScope(Dispatchers.Main)
            scope.launch { ran = true }

            assertFalse(ran)
            dispatcher.scheduler.advanceUntilIdle()
            assertTrue(ran)
            scope.cancel()
        }
        assertEquals(dispatcher, rule.testDispatcher)
    }

    @Test
    fun `resets Main after the test`() {
        MainDispatcherRule().run { }

        // On the JVM there's no real Main dispatcher, so once reset it fails as soon as it's used.
        assertThrows(IllegalStateException::class.java) { useMain() }
    }

    @Test
    fun `resets Main even when the test fails`() {
        assertThrows(AssertionError::class.java) {
            MainDispatcherRule().run { throw AssertionError("test failed") }
        }

        assertThrows(IllegalStateException::class.java) { useMain() }
    }

    private fun useMain() = runBlocking { withContext(Dispatchers.Main) { } }
}
