package com.dev.fxwatch.core.domain.result

/**
 * Outcome of a domain operation: either [Success] with its [Success.data] or [Failure] with a typed [DomainError].
 *
 * Unlike `kotlin.Result`, whose failure is an untyped `Throwable`, the error here is a closed hierarchy,
 * so a `when` over it is exhaustive and the compiler flags any unhandled case.
 */
sealed interface DomainResult<out T> {

    data class Success<out T>(val data: T) : DomainResult<T>

    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

fun <T> T.asSuccess(): DomainResult<T> = DomainResult.Success(this)

fun DomainError.asFailure(): DomainResult<Nothing> = DomainResult.Failure(this)

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(data))
    is DomainResult.Failure -> this
}

/** Chains another operation that can fail; the first failure short-circuits. */
inline fun <T, R> DomainResult<T>.flatMap(transform: (T) -> DomainResult<R>): DomainResult<R> = when (this) {
    is DomainResult.Success -> transform(data)
    is DomainResult.Failure -> this
}

inline fun <T, R> DomainResult<T>.fold(onSuccess: (T) -> R, onFailure: (DomainError) -> R): R = when (this) {
    is DomainResult.Success -> onSuccess(data)
    is DomainResult.Failure -> onFailure(error)
}

inline fun <T> DomainResult<T>.onSuccess(action: (T) -> Unit): DomainResult<T> = also {
    if (it is DomainResult.Success) action(it.data)
}

inline fun <T> DomainResult<T>.onFailure(action: (DomainError) -> Unit): DomainResult<T> = also {
    if (it is DomainResult.Failure) action(it.error)
}

fun <T> DomainResult<T>.getOrNull(): T? = (this as? DomainResult.Success)?.data

fun DomainResult<*>.errorOrNull(): DomainError? = (this as? DomainResult.Failure)?.error
