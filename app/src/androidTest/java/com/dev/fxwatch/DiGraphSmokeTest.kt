package com.dev.fxwatch

import com.dev.fxwatch.core.domain.di.DefaultDispatcher
import com.dev.fxwatch.core.domain.di.IoDispatcher
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Requests the base bindings, because Dagger doesn't validate bindings that nobody asks for. */
@HiltAndroidTest
class DiGraphSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var clock: Clock

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    @DefaultDispatcher
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun graphProvidesClockAndDispatchers() {
        assertNotNull(clock.instant())
        assertEquals(Dispatchers.IO, ioDispatcher)
        assertEquals(Dispatchers.Default, defaultDispatcher)
    }
}
