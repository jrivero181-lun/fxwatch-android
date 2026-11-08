package com.dev.fxwatch.core.domain.usecase

import app.cash.turbine.test
import com.dev.fxwatch.core.domain.result.DomainError
import com.dev.fxwatch.core.domain.result.DomainResult
import com.dev.fxwatch.core.testing.repository.FakeFavoritesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    @Test
    fun `adds a currency that is not a favorite`() = runTest {
        val repository = FakeFavoritesRepository(setOf("GBP"))
        val useCase = ToggleFavoriteUseCase(repository)

        repository.observeFavorites().test {
            assertEquals(setOf("GBP"), awaitItem())

            assertEquals(DomainResult.Success(true), useCase("USD"))

            assertEquals(setOf("GBP", "USD"), awaitItem())
        }
    }

    @Test
    fun `removes a currency that is already a favorite`() = runTest {
        val repository = FakeFavoritesRepository(setOf("GBP", "USD"))
        val useCase = ToggleFavoriteUseCase(repository)

        repository.observeFavorites().test {
            assertEquals(setOf("GBP", "USD"), awaitItem())

            assertEquals(DomainResult.Success(false), useCase("USD"))

            assertEquals(setOf("GBP"), awaitItem())
        }
    }

    @Test
    fun `toggling twice restores the original state`() = runTest {
        val repository = FakeFavoritesRepository()
        val useCase = ToggleFavoriteUseCase(repository)

        assertEquals(DomainResult.Success(true), useCase("JPY"))
        assertEquals(DomainResult.Success(false), useCase("JPY"))

        repository.observeFavorites().test {
            assertEquals(emptySet<String>(), awaitItem())
        }
    }

    @Test
    fun `propagates storage failures and leaves favorites untouched`() = runTest {
        val repository = FakeFavoritesRepository(setOf("GBP"))
        repository.error = DomainError.Storage
        val useCase = ToggleFavoriteUseCase(repository)

        repository.observeFavorites().test {
            assertEquals(setOf("GBP"), awaitItem())

            assertEquals(DomainResult.Failure(DomainError.Storage), useCase("USD"))

            expectNoEvents()
        }
    }
}
