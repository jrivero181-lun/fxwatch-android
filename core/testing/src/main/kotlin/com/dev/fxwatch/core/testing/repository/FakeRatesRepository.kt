package com.dev.fxwatch.core.testing.repository

import com.dev.fxwatch.core.domain.model.Currency
import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.repository.RatesRepository
import java.time.LocalDate

/**
 * In-memory [RatesRepository]. Seed it through its public properties; set [error] to make every call throw.
 * The last history request is recorded in [lastHistoryRequest].
 */
class FakeRatesRepository : RatesRepository {

    var currencies: List<Currency> = emptyList()

    /** Rates keyed by base currency. */
    var latestRates: Map<String, List<ExchangeRate>> = emptyMap()

    /** History points returned for every request; filtered by the requested date range. */
    var history: RateHistory? = null

    var error: Throwable? = null

    var lastHistoryRequest: HistoryRequest? = null
        private set

    override suspend fun getCurrencies(): List<Currency> {
        throwIfNeeded()
        return currencies
    }

    override suspend fun getLatestRates(base: String): List<ExchangeRate> {
        throwIfNeeded()
        return latestRates[base].orEmpty()
    }

    override suspend fun getRateHistory(base: String, quote: String, from: LocalDate, to: LocalDate): RateHistory {
        lastHistoryRequest = HistoryRequest(base, quote, from, to)
        throwIfNeeded()
        val points = history?.points.orEmpty().filter { it.date in from..to }
        return RateHistory(base = base, quote = quote, points = points)
    }

    private fun throwIfNeeded() {
        error?.let { throw it }
    }

    data class HistoryRequest(val base: String, val quote: String, val from: LocalDate, val to: LocalDate)
}
