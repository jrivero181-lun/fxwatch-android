package com.dev.fxwatch.core.domain.repository

import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow

/**
 * Persists the currency codes the user marked as favorites.
 * Write failures are reported as [DomainError.Storage].
 */
interface FavoritesRepository {

    /** Emits the current set of favorite codes and every subsequent change. */
    fun observeFavorites(): Flow<Set<String>>

    suspend fun addFavorite(code: String): DomainResult<Unit>

    suspend fun removeFavorite(code: String): DomainResult<Unit>
}
