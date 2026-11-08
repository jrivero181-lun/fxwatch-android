package com.dev.fxwatch.core.testing.repository

import com.dev.fxwatch.core.domain.model.Currency
import com.dev.fxwatch.core.domain.model.CurrencyType
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.model.RatePoint
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import java.math.BigDecimal
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeRatesRepositoryTest {

    private val repository = FakeRatesRepository()
    private val day = LocalDate.of(2026, 11, 1)

    private fun point(offsetDays: Long) = RatePoint(day.plusDays(offsetDays), BigDecimal("4.3"))

    @Test
    fun `history keeps only points inside the requested range, both ends inclusive`() = runTest {
        repository.history = RateHistory("EUR", "AED", (0L..6L).map(::point))

        val result = repository.getRateHistory("EUR", "AED", from = day.plusDays(2), to = day.plusDays(4))

        assertEquals(DomainResult.Success(RateHistory("EUR", "AED", (2L..4L).map(::point))), result)
    }

    @Test
    fun `history uses the requested pair, not the seeded one`() = runTest {
        repository.history = RateHistory("EUR", "AED", listOf(point(0)))

        val result = repository.getRateHistory("USD", "GBP", day, day)

        assertEquals(DomainResult.Success(RateHistory("USD", "GBP", listOf(point(0)))), result)
    }

    @Test
    fun `getCurrencies returns the seeded currencies`() = runTest {
        val aave = Currency(code = "AAVE", name = "Aave", symbol = "AAVE", type = CurrencyType.CRYPTO)
        repository.currencies = listOf(aave)

        assertEquals(DomainResult.Success(listOf(aave)), repository.getCurrencies())
    }

    @Test
    fun `error makes every call fail and history requests are still recorded`() = runTest {
        repository.error = DomainError.Network
        val failure = DomainResult.Failure(DomainError.Network)

        assertEquals(failure, repository.getCurrencies())
        assertEquals(failure, repository.getLatestRates("EUR"))
        assertEquals(failure, repository.getRateHistory("EUR", "AED", day, day))
        assertEquals(FakeRatesRepository.HistoryRequest("EUR", "AED", day, day), repository.lastHistoryRequest)
    }
}
