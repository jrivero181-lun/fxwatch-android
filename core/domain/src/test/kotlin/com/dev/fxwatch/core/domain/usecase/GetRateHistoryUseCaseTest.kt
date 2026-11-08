package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.HistoryPeriod
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.model.RatePoint
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.getOrNull
import com.dev.fxwatch.core.testing.repository.FakeRatesRepository
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetRateHistoryUseCaseTest {

    private val today = LocalDate.of(2026, 11, 8)
    private val clock = Clock.fixed(today.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC)
    private val repository = FakeRatesRepository()
    private val useCase = GetRateHistoryUseCase(repository, clock)

    private fun point(date: LocalDate, rate: String) = RatePoint(date, BigDecimal(rate))

    @Test
    fun `requests the period ending today`() = runTest {
        useCase("EUR", "AED", HistoryPeriod.MONTH)

        assertEquals(
            FakeRatesRepository.HistoryRequest("EUR", "AED", today.minusDays(30), today),
            repository.lastHistoryRequest
        )
    }

    @Test
    fun `returns points sorted by ascending date`() = runTest {
        val points = listOf(
            point(today, "4.30"),
            point(today.minusDays(2), "4.313"),
            point(today.minusDays(1), "4.29")
        )
        repository.history = RateHistory("EUR", "AED", points)

        val history = useCase("EUR", "AED", HistoryPeriod.WEEK).getOrNull()

        assertEquals(listOf(today.minusDays(2), today.minusDays(1), today), history?.points?.map { it.date })
        assertEquals(point(today, "4.30"), history?.latest)
    }

    @Test
    fun `empty history has no latest point`() = runTest {
        val result = useCase("EUR", "AED", HistoryPeriod.YEAR)

        assertEquals(DomainResult.Success(RateHistory("EUR", "AED", emptyList())), result)
        assertNull(result.getOrNull()?.latest)
    }

    @Test
    fun `fails with SameCurrency without hitting the repository`() = runTest {
        assertEquals(DomainResult.Failure(DomainError.SameCurrency("EUR")), useCase("EUR", "EUR", HistoryPeriod.WEEK))
        assertNull(repository.lastHistoryRequest)
    }

    @Test
    fun `propagates repository failures`() = runTest {
        repository.error = DomainError.Server(code = 503)

        assertEquals(DomainResult.Failure(DomainError.Server(503)), useCase("EUR", "AED", HistoryPeriod.WEEK))
    }
}
