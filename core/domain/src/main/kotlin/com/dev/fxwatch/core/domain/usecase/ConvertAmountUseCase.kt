package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.model.ExchangeRate
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.asFailure
import com.dev.fxwatch.core.domain.result.asSuccess
import java.math.BigDecimal
import java.math.MathContext

/**
 * Converts [amount] from one currency to another using a set of rates that share a common base.
 *
 * Neither currency has to be the base: the result goes through a cross rate
 * (`amount * rate(to) / rate(from)`), with the base itself counting as rate 1.
 * It's synchronous and pure so the converter can recompute on every keystroke without I/O.
 */
class ConvertAmountUseCase {

    /**
     * @param rates rates that all share the same base; mixing bases is a programming error and throws.
     * @return the converted amount, [DomainError.NegativeAmount] for a negative [amount], or
     * [DomainError.RateUnavailable] when [rates] lacks a usable rate for [from] or [to].
     */
    operator fun invoke(
        amount: BigDecimal,
        from: String,
        to: String,
        rates: List<ExchangeRate>
    ): DomainResult<BigDecimal> {
        val rateByCode = rateByCode(rates)
        val fromRate = rateByCode[from]?.takeIf { it.signum() != 0 }
        val toRate = rateByCode[to]
        return when {
            amount.signum() < 0 -> DomainError.NegativeAmount.asFailure()
            from == to -> amount.asSuccess()
            fromRate == null -> DomainError.RateUnavailable(from).asFailure()
            toRate == null -> DomainError.RateUnavailable(to).asFailure()
            else -> amount.multiply(toRate).divide(fromRate, MathContext.DECIMAL64).asSuccess()
        }
    }

    /** Rate of every code against the common base, including the base itself (rate 1). */
    private fun rateByCode(rates: List<ExchangeRate>): Map<String, BigDecimal> {
        val base = rates.firstOrNull()?.base ?: return emptyMap()
        require(rates.all { it.base == base }) { "All rates must share the same base" }
        return rates.associate { it.quote to it.rate } + (base to BigDecimal.ONE)
    }
}
