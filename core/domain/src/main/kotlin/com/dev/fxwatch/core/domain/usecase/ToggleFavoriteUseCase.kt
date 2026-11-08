package com.dev.fxwatch.core.domain.usecase

import com.dev.fxwatch.core.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.first

/** Adds [code] to the favorites if it isn't there, removes it otherwise. */
class ToggleFavoriteUseCase(private val favoritesRepository: FavoritesRepository) {
    /** @return `true` if [code] is a favorite after the toggle. */
    suspend operator fun invoke(code: String): Boolean {
        val isFavorite = code in favoritesRepository.observeFavorites().first()
        if (isFavorite) {
            favoritesRepository.removeFavorite(code)
        } else {
            favoritesRepository.addFavorite(code)
        }
        return !isFavorite
    }
}
