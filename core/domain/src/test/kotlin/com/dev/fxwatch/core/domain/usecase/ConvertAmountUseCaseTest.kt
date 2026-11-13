package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertAmountUseCaseTest {

    private val useCase = ConvertAmountUseCase()
    private val date = LocalDate.of(2026, 11, 7)
    private val rates = listOf(
        ExchangeRate(date, "EUR", "USD", BigDecimal("1.25")),
        ExchangeRate(date, "EUR", "GBP", BigDecimal("0.5"))
    )

    /** Compares by value, ignoring scale (`40` == `40.00`). */
    private fun assertAmount(expected: String, actual: DomainResult<BigDecimal>) {
        assertTrue("Expected Success but was $actual", actual is DomainResult.Success)
        assertEquals(0, BigDecimal(expected).compareTo((actual as DomainResult.Success).data))
    }

    private fun assertFailure(expected: DomainError, actual: DomainResult<BigDecimal>) {
        assertEquals(DomainResult.Failure(expected), actual)
    }

    @Test
    fun `converts from base to quote`() {
        assertAmount("125", useCase(BigDecimal("100"), "EUR", "USD", rates))
    }

    @Test
    fun `converts from quote to base`() {
        assertAmount("80", useCase(BigDecimal("100"), "USD", "EUR", rates))
    }

    @Test
    fun `converts between two quotes through a cross rate`() {
        // 100 USD = 80 EUR = 40 GBP
        assertAmount("40", useCase(BigDecimal("100"), "USD", "GBP", rates))
    }

    @Test
    fun `same currency returns the same amount even without rates`() {
        assertAmount("42.5", useCase(BigDecimal("42.5"), "USD", "USD", emptyList()))
    }

    @Test
    fun `zero amount converts to zero`() {
        assertAmount("0", useCase(BigDecimal.ZERO, "EUR", "USD", rates))
    }

    @Test
    fun `fails with RateUnavailable naming the missing currency`() {
        assertFailure(DomainError.RateUnavailable("JPY"), useCase(BigDecimal.ONE, "EUR", "JPY", rates))
        assertFailure(DomainError.RateUnavailable("JPY"), useCase(BigDecimal.ONE, "JPY", "EUR", rates))
        assertFailure(DomainError.RateUnavailable("EUR"), useCase(BigDecimal.ONE, "EUR", "USD", emptyList()))
    }

    @Test
    fun `fails with RateUnavailable when the source rate is zero`() {
        val withZero = rates + ExchangeRate(date, "EUR", "XXX", BigDecimal.ZERO)

        assertFailure(DomainError.RateUnavailable("XXX"), useCase(BigDecimal.ONE, "XXX", "USD", withZero))
    }

    @Test
    fun `fails with NegativeAmount for negative amounts`() {
        assertFailure(DomainError.NegativeAmount, useCase(BigDecimal("-1"), "EUR", "USD", rates))
    }

    @Test
    fun `non-terminating results are rounded to 16 significant digits`() {
        val thirds = listOf(ExchangeRate(date, "EUR", "XXX", BigDecimal("3")))

        // 1 XXX = 1/3 EUR: periodic, must be rounded instead of throwing ArithmeticException
        assertEquals(
            DomainResult.Success(BigDecimal("0.3333333333333333")),
            useCase(BigDecimal.ONE, "XXX", "EUR", thirds)
        )
    }

    @Test
    fun `ties are rounded half-even`() {
        val parity = listOf(ExchangeRate(date, "EUR", "XXX", BigDecimal.ONE))

        // 17 significant digits ending in 5: exact ties at the 16th digit
        assertEquals(
            DomainResult.Success(BigDecimal("0.1234567890123456")),
            useCase(BigDecimal("0.12345678901234565"), "EUR", "XXX", parity)
        )
        assertEquals(
            DomainResult.Success(BigDecimal("0.1234567890123458")),
            useCase(BigDecimal("0.12345678901234575"), "EUR", "XXX", parity)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `throws for rates with different bases`() {
        val mixed = rates + ExchangeRate(date, "USD", "JPY", BigDecimal("150"))

        useCase(BigDecimal.ONE, "EUR", "USD", mixed)
    }
}
