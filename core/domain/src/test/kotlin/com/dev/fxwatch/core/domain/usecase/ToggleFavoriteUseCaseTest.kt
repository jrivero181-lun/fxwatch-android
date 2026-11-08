package com.dev.fxwatch.core.domain.usecase

import app.cash.turbine.test
import com.dev.fxwatch.core.testing.repository.FakeFavoritesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    @Test
    fun `adds a currency that is not a favorite`() = runTest {
        val repository = FakeFavoritesRepository(setOf("GBP"))
        val useCase = ToggleFavoriteUseCase(repository)

        repository.observeFavorites().test {
            assertEquals(setOf("GBP"), awaitItem())

            assertTrue(useCase("USD"))

            assertEquals(setOf("GBP", "USD"), awaitItem())
        }
    }

    @Test
    fun `removes a currency that is already a favorite`() = runTest {
        val repository = FakeFavoritesRepository(setOf("GBP", "USD"))
        val useCase = ToggleFavoriteUseCase(repository)

        repository.observeFavorites().test {
            assertEquals(setOf("GBP", "USD"), awaitItem())

            assertFalse(useCase("USD"))

            assertEquals(setOf("GBP"), awaitItem())
        }
    }

    @Test
    fun `toggling twice restores the original state`() = runTest {
        val repository = FakeFavoritesRepository()
        val useCase = ToggleFavoriteUseCase(repository)

        assertTrue(useCase("JPY"))
        assertFalse(useCase("JPY"))

        repository.observeFavorites().test {
            assertEquals(emptySet<String>(), awaitItem())
        }
    }
}
