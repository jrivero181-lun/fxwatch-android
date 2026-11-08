package com.dev.fxwatch.core.domain.model

/**
 * A currency that can be quoted, either a fiat currency or a crypto asset.
 *
 * @property code ISO 4217 code for fiat currencies or ticker for crypto assets (e.g. `EUR`, `AAVE`).
 * @property isoNumeric ISO 4217 numeric code; `null` for currencies that don't have one (e.g. crypto).
 * @property symbol display symbol (e.g. `€`); `null` when the source doesn't provide one.
 */
data class Currency(
    val code: String,
    val name: String,
    val symbol: String?,
    val type: CurrencyType,
    val isoNumeric: Int? = null
)

enum class CurrencyType {
    FIAT,
    CRYPTO,

    /** Any other kind of asset reported by the source (e.g. precious metals). */
    OTHER
}
