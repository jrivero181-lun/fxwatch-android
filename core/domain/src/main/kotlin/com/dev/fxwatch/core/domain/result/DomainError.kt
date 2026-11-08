package com.dev.fxwatch.core.domain.result

/**
 * Every expected failure the domain can report. The hierarchy is sealed so callers handle
 * each case exhaustively in a `when`, typically to map it to a UI message.
 *
 * Programming errors (broken contracts such as rates with mixed bases) are not modeled here:
 * they still throw.
 */
sealed interface DomainError {

    /** Failures coming from the data sources. The data layer maps its exceptions to these. */
    sealed interface Data : DomainError

    /** The request didn't reach the server (no connection, timeout...). */
    data object Network : Data

    /** The server answered with an error. [code] is the HTTP status, if any. */
    data class Server(val code: Int? = null) : Data

    /** Local persistence (e.g. favorites) couldn't be read or written. */
    data object Storage : Data

    /** The source doesn't know [code]. */
    data class UnsupportedCurrency(val code: String) : Data

    /** Any other data failure. [cause] is kept for logging, never for display. */
    data class Unknown(val cause: Throwable? = null) : Data

    /** Invalid input or a request the domain can't fulfil with the data at hand. */
    sealed interface Validation : DomainError

    /** The amount to convert is negative. */
    data object NegativeAmount : Validation

    /** Base and quote are the same currency, so there's no rate to show. */
    data class SameCurrency(val code: String) : Validation

    /** There's no usable (present and non-zero) rate for [code]. */
    data class RateUnavailable(val code: String) : Validation
}
