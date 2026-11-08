package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.testing.repository.FakeRatesRepository
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLatestRatesUseCaseTest {

    private val repository = FakeRatesRepository()
    private val useCase = GetLatestRatesUseCase(repository)
    private val date = LocalDate.of(2026, 11, 7)

    private fun rate(quote: String, value: String) = ExchangeRate(date, "EUR", quote, BigDecimal(value))

    @Test
    fun `returns rates sorted by quote`() = runTest {
        repository.latestRates =
            mapOf("EUR" to listOf(rate("USD", "1.08"), rate("AAVE", "0.0085519"), rate("GBP", "0.86")))

        val result = useCase("EUR")

        assertEquals(listOf("AAVE", "GBP", "USD"), result.map { it.quote })
    }

    @Test
    fun `drops the rate of the base against itself`() = runTest {
        repository.latestRates = mapOf("EUR" to listOf(rate("EUR", "1"), rate("USD", "1.08")))

        val result = useCase("EUR")

        assertEquals(listOf("USD"), result.map { it.quote })
    }

    @Test
    fun `returns empty list when the base has no rates`() = runTest {
        assertTrue(useCase("XYZ").isEmpty())
    }

    @Test(expected = IOException::class)
    fun `propagates repository errors`() = runTest {
        repository.error = IOException("offline")

        useCase("EUR")
    }
}
