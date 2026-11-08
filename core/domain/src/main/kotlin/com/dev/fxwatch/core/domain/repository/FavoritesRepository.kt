package com.dev.fxwatch.core.domain.repository

import kotlinx.coroutines.flow.Flow

/** Persists the currency codes the user marked as favorites. */
interface FavoritesRepository {

    /** Emits the current set of favorite codes and every subsequent change. */
    fun observeFavorites(): Flow<Set<String>>

    suspend fun addFavorite(code: String)

    suspend fun removeFavorite(code: String)
}
