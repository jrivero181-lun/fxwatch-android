package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.repository.FavoritesRepository
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.domain.result.map
import kotlinx.coroutines.flow.first

/** Adds [code] to the favorites if it isn't there, removes it otherwise. */
class ToggleFavoriteUseCase(private val favoritesRepository: FavoritesRepository) {
    /** @return `true` if [code] is a favorite after the toggle, or the storage failure. */
    suspend operator fun invoke(code: String): DomainResult<Boolean> {
        val isFavorite = code in favoritesRepository.observeFavorites().first()
        val result = if (isFavorite) {
            favoritesRepository.removeFavorite(code)
        } else {
            favoritesRepository.addFavorite(code)
        }
        return result.map { !isFavorite }
    }
}
