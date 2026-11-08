package com.dev.fxwatch.core.domain.repository

import com.dev.fxwatch.core.domain.model.Currency
import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.model.RateHistory
import java.time.LocalDate

/**
 * Source of currencies and exchange rates. Implementations live in the data layer and
 * signal failures (network, parsing...) by throwing.
 */
interface RatesRepository {

    suspend fun getCurrencies(): List<Currency>

    /** Latest available rates of [base] against every quote currency the source supports. */
    suspend fun getLatestRates(base: String): List<ExchangeRate>

    /** Daily [base]/[quote] rates between [from] and [to], both inclusive. */
    suspend fun getRateHistory(base: String, quote: String, from: LocalDate, to: LocalDate): RateHistory
}
