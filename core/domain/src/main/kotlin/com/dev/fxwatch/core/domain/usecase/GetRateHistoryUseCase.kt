package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.HistoryPeriod
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.repository.RatesRepository
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.asFailure
import com.dev.fxwatch.core.domain.result.map
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
    /** @return the history, [DomainError.SameCurrency] if [base] equals [quote], or the data failure. */
    suspend operator fun invoke(base: String, quote: String, period: HistoryPeriod): DomainResult<RateHistory> {
        if (base == quote) return DomainError.SameCurrency(base).asFailure()
        val to = LocalDate.now(clock)
        val from = to.minusDays(period.days)
        return ratesRepository.getRateHistory(base, quote, from, to).map { history ->
            history.copy(points = history.points.sortedBy { it.date })
        }
    }
}
