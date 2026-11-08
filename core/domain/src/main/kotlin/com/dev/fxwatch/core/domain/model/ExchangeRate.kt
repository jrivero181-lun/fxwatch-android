package com.dev.fxwatch.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Price of one unit of [base] expressed in [quote] on [date]: `1 base = rate quote`.
 *
 * [rate] is a [BigDecimal] so conversions don't accumulate floating-point errors.
 */
data class ExchangeRate(val date: LocalDate, val base: String, val quote: String, val rate: BigDecimal)
