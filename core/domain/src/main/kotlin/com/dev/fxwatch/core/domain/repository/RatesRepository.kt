package com.dev.fxwatch.core.domain.repository

import com.dev.fxwatch.core.domain.model.Currency
import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.model.RateHistory
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import java.time.LocalDate

/**
 * Source of currencies and exchange rates. Implementations live in the data layer, must not throw
 * for expected failures, and report them as a [DomainError.Data] inside [DomainResult.Failure].
 */
interface RatesRepository {

    suspend fun getCurrencies(): DomainResult<List<Currency>>

    /** Latest available rates of [base] against every quote currency the source supports. */
    suspend fun getLatestRates(base: String): DomainResult<List<ExchangeRate>>

    /** Daily [base]/[quote] rates between [from] and [to], both inclusive. */
    suspend fun getRateHistory(base: String, quote: String, from: LocalDate, to: LocalDate): DomainResult<RateHistory>
}
