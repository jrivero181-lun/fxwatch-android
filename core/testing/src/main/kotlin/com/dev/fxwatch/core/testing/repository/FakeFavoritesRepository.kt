package com.dev.fxwatch.core.testing.repository

import com.dev.fxwatch.core.domain.repository.FavoritesRepository
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.asFailure
import com.dev.fxwatch.core.domain.result.asSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * In-memory [FavoritesRepository] backed by a [MutableStateFlow], so observers see every change.
 * Set [error] to make writes fail without changing the favorites.
 */
class FakeFavoritesRepository(initial: Set<String> = emptySet()) : FavoritesRepository {

    private val favorites = MutableStateFlow(initial)

    var error: DomainError? = null

    override fun observeFavorites(): Flow<Set<String>> = favorites

    override suspend fun addFavorite(code: String): DomainResult<Unit> = write { it + code }

    override suspend fun removeFavorite(code: String): DomainResult<Unit> = write { it - code }

    private fun write(change: (Set<String>) -> Set<String>): DomainResult<Unit> =
        error?.asFailure() ?: favorites.update(change).asSuccess()
}
