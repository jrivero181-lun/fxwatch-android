package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.testing.repository.FakeRatesRepository
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetLatestRatesUseCaseTest {

    private val repository = FakeRatesRepository()
    private val useCase = GetLatestRatesUseCase(repository)
    private val date = LocalDate.of(2026, 11, 7)

    private fun rate(quote: String, value: String) = ExchangeRate(date, "EUR", quote, BigDecimal(value))

    @Test
    fun `returns rates sorted by quote`() = runTest {
        val usd = rate("USD", "1.08")
        val aave = rate("AAVE", "0.0085519")
        val gbp = rate("GBP", "0.86")
        repository.latestRates = mapOf("EUR" to listOf(usd, aave, gbp))

        assertEquals(DomainResult.Success(listOf(aave, gbp, usd)), useCase("EUR"))
    }

    @Test
    fun `drops the rate of the base against itself`() = runTest {
        val usd = rate("USD", "1.08")
        repository.latestRates = mapOf("EUR" to listOf(rate("EUR", "1"), usd))

        assertEquals(DomainResult.Success(listOf(usd)), useCase("EUR"))
    }

    @Test
    fun `returns an empty list when the base has no rates`() = runTest {
        assertEquals(DomainResult.Success(emptyList<ExchangeRate>()), useCase("XYZ"))
    }

    @Test
    fun `propagates repository failures`() = runTest {
        repository.error = DomainError.Network

        assertEquals(DomainResult.Failure(DomainError.Network), useCase("EUR"))
    }
}
