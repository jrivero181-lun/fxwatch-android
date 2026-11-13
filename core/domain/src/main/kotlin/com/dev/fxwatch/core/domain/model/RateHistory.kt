package com.dev.fxwatch.core.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/** Time series of the [base]/[quote] rate, with [points] sorted by ascending date. */
data class RateHistory(val base: String, val quote: String, val points: List<RatePoint>) {
    val latest: RatePoint? get() = points.lastOrNull()
}

data class RatePoint(val date: LocalDate, val rate: BigDecimal)

/** Time windows offered for a rate history, measured back from today. */
enum class HistoryPeriod(val days: Long) {
    WEEK(7),
    MONTH(30),
    QUARTER(90),
    YEAR(365)
}
