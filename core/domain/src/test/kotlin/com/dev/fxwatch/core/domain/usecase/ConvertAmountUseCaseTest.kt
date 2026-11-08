package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import java.math.BigDecimal
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertAmountUseCaseTest {

    private val useCase = ConvertAmountUseCase()
    private val date = LocalDate.of(2026, 11, 7)
    private val rates = listOf(
        ExchangeRate(date, "EUR", "USD", BigDecimal("1.25")),
        ExchangeRate(date, "EUR", "GBP", BigDecimal("0.5"))
    )

    private fun assertAmount(expected: String, actual: BigDecimal?) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
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
    fun `returns null when a rate is missing`() {
        assertNull(useCase(BigDecimal.ONE, "EUR", "JPY", rates))
        assertNull(useCase(BigDecimal.ONE, "JPY", "EUR", rates))
        assertNull(useCase(BigDecimal.ONE, "EUR", "USD", emptyList()))
    }

    @Test
    fun `returns null when the source rate is zero`() {
        val withZero = rates + ExchangeRate(date, "EUR", "XXX", BigDecimal.ZERO)

        assertNull(useCase(BigDecimal.ONE, "XXX", "USD", withZero))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects negative amounts`() {
        useCase(BigDecimal("-1"), "EUR", "USD", rates)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects rates with different bases`() {
        val mixed = rates + ExchangeRate(date, "USD", "JPY", BigDecimal("150"))

        useCase(BigDecimal.ONE, "EUR", "USD", mixed)
    }
}
