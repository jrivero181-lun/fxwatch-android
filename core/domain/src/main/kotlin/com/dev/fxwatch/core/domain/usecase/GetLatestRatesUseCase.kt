package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.repository.RatesRepository
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.map

/** Latest rates of [base] against every other currency, without the trivial `base/base` rate, sorted by quote. */
class GetLatestRatesUseCase(private val ratesRepository: RatesRepository) {
    suspend operator fun invoke(base: String): DomainResult<List<ExchangeRate>> =
        ratesRepository.getLatestRates(base).map { rates ->
            rates.filter { it.quote != base }.sortedBy { it.quote }
        }
}
