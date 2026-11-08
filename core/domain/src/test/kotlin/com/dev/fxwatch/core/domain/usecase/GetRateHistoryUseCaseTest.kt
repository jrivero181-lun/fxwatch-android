package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.HistoryPeriod
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.model.RatePoint
import com.dev.fxwatch.core.testing.repository.FakeRatesRepository
import java.io.IOException
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

        val result = useCase("EUR", "AED", HistoryPeriod.WEEK)

        assertEquals(listOf(today.minusDays(2), today.minusDays(1), today), result.points.map { it.date })
        assertEquals(point(today, "4.30"), result.latest)
    }

    @Test
    fun `empty history has no latest point`() = runTest {
        val result = useCase("EUR", "AED", HistoryPeriod.YEAR)

        assertEquals(emptyList<RatePoint>(), result.points)
        assertNull(result.latest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects identical base and quote`() = runTest {
        useCase("EUR", "EUR", HistoryPeriod.WEEK)
    }

    @Test(expected = IOException::class)
    fun `propagates repository errors`() = runTest {
        repository.error = IOException("offline")

        useCase("EUR", "AED", HistoryPeriod.WEEK)
    }
}
