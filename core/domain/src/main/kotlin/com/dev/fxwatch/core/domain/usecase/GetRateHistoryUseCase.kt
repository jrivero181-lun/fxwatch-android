package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.HistoryPeriod
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.repository.RatesRepository
import java.time.Clock
import java.time.LocalDate

/**
 * History of the [base]/[quote] rate over a [HistoryPeriod] ending today, sorted by ascending date.
 *
 * [clock] defines "today" and is injectable so tests are deterministic.
 */
class GetRateHistoryUseCase(
    private val ratesRepository: RatesRepository,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    suspend operator fun invoke(base: String, quote: String, period: HistoryPeriod): RateHistory {
        require(base != quote) { "Base and quote must differ: $base" }
        val to = LocalDate.now(clock)
        val from = to.minusDays(period.days)
        val history = ratesRepository.getRateHistory(base, quote, from, to)
        return history.copy(points = history.points.sortedBy { it.date })
    }
}
