package com.dev.fxwatch.core.domain.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DomainResultTest {

    private val success: DomainResult<Int> = 2.asSuccess()
    private val failure: DomainResult<Int> = DomainError.Network.asFailure()

    @Test
    fun `map transforms success and keeps failure`() {
        assertEquals(DomainResult.Success(4), success.map { it * 2 })
        assertEquals(failure, failure.map { it * 2 })
    }

    @Test
    fun `flatMap chains success and short-circuits on the first failure`() {
        assertEquals(DomainResult.Success("2"), success.flatMap { it.toString().asSuccess() })
        assertEquals(
            DomainResult.Failure(DomainError.Storage),
            success.flatMap { DomainError.Storage.asFailure() }
        )

        var called = false
        val result = failure.flatMap {
            called = true
            it.asSuccess()
        }
        assertEquals(failure, result)
        assertEquals(false, called)
    }

    @Test
    fun `fold picks the branch matching the outcome`() {
        assertEquals("ok 2", success.fold(onSuccess = { "ok $it" }, onFailure = { "ko $it" }))
        assertEquals("ko Network", failure.fold(onSuccess = { "ok $it" }, onFailure = { "ko $it" }))
    }

    @Test
    fun `onSuccess and onFailure run only on their outcome and return the receiver`() {
        val seen = mutableListOf<Any>()

        assertEquals(success, success.onSuccess { seen += it }.onFailure { seen += it })
        assertEquals(failure, failure.onSuccess { seen += it }.onFailure { seen += it })

        assertEquals(listOf<Any>(2, DomainError.Network), seen)
    }

    @Test
    fun `getOrNull and errorOrNull expose the matching side`() {
        assertEquals(2, success.getOrNull())
        assertNull(success.errorOrNull())
        assertNull(failure.getOrNull())
        assertEquals(DomainError.Network, failure.errorOrNull())
    }
}
