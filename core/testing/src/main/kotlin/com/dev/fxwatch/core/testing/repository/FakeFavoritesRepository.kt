package com.dev.fxwatch.core.testing.repository

import com.dev.fxwatch.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory [FavoritesRepository] backed by a [MutableStateFlow], so observers see every change. */
class FakeFavoritesRepository(initial: Set<String> = emptySet()) : FavoritesRepository {

    private val favorites = MutableStateFlow(initial)

    override fun observeFavorites(): Flow<Set<String>> = favorites

    override suspend fun addFavorite(code: String) {
        favorites.update { it + code }
    }

    override suspend fun removeFavorite(code: String) {
        favorites.update { it - code }
    }
}
