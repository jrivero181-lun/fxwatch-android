package com.dev.fxwatch.core.testing.repository

import com.dev.fxwatch.core.domain.model.Currency
import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.repository.RatesRepository
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.asFailure
import com.dev.fxwatch.core.domain.result.asSuccess
import java.time.LocalDate

/**
 * In-memory [RatesRepository]. Seed it through its public properties; set [error] to make every call fail with it.
 * The last history request is recorded in [lastHistoryRequest].
 */
class FakeRatesRepository : RatesRepository {

    var currencies: List<Currency> = emptyList()

    /** Rates keyed by base currency. */
    var latestRates: Map<String, List<ExchangeRate>> = emptyMap()

    /** History points returned for every request; filtered by the requested date range. */
    var history: RateHistory? = null

    var error: DomainError? = null

    var lastHistoryRequest: HistoryRequest? = null
        private set

    override suspend fun getCurrencies(): DomainResult<List<Currency>> = resultOf { currencies }

    override suspend fun getLatestRates(base: String): DomainResult<List<ExchangeRate>> =
        resultOf { latestRates[base].orEmpty() }

    override suspend fun getRateHistory(
        base: String,
        quote: String,
        from: LocalDate,
        to: LocalDate
    ): DomainResult<RateHistory> {
        lastHistoryRequest = HistoryRequest(base, quote, from, to)
        return resultOf {
            val points = history?.points.orEmpty().filter { it.date in from..to }
            RateHistory(base = base, quote = quote, points = points)
        }
    }

    private inline fun <T> resultOf(data: () -> T): DomainResult<T> = error?.asFailure() ?: data().asSuccess()

    data class HistoryRequest(val base: String, val quote: String, val from: LocalDate, val to: LocalDate)
}
