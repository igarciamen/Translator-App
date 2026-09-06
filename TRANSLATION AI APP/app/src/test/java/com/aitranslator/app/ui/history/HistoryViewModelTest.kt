package com.aitranslator.app.ui.history

import com.aitranslator.app.domain.history.HistoryEntry
import com.aitranslator.app.domain.history.HistoryRepository
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.util.MainDispatcherRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeHistoryRepository : HistoryRepository {
    val historyFlow = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val favoritesFlow = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val deletedIds = mutableListOf<Long>()
    val toggledFavorites = mutableListOf<Pair<Long, Boolean>>()
    var clearCalled = false

    override suspend fun saveTranslation(
        sourceText: String,
        translatedText: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ) = Unit

    override fun observeHistory() = historyFlow

    override fun observeFavorites() = favoritesFlow

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        toggledFavorites.add(id to isFavorite)
    }

    override suspend fun deleteEntry(id: Long) {
        deletedIds.add(id)
        historyFlow.value = historyFlow.value.filterNot { it.id == id }
    }

    override suspend fun clearHistory() {
        clearCalled = true
        historyFlow.value = emptyList()
    }
}

class HistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun sampleEntry(id: Long, favorite: Boolean = false) = HistoryEntry(
        id = id,
        sourceText = "Hola",
        translatedText = "Hello",
        sourceLanguage = Language.SPANISH,
        targetLanguage = Language.ENGLISH,
        timestampMillis = 1000L,
        isFavorite = favorite
    )

    @Test
    fun `state shows the full history by default`() = runTest {
        val repository = FakeHistoryRepository()
        repository.historyFlow.value = listOf(sampleEntry(1L), sampleEntry(2L))
        val viewModel = HistoryViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.showFavoritesOnly)
        assertEquals(2, state.entries.size)
    }

    @Test
    fun `enabling favorites-only switches the displayed list to favorites`() = runTest {
        val repository = FakeHistoryRepository()
        repository.historyFlow.value = listOf(sampleEntry(1L), sampleEntry(2L, favorite = true))
        repository.favoritesFlow.value = listOf(sampleEntry(2L, favorite = true))
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.onShowFavoritesOnlyToggled(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showFavoritesOnly)
        assertEquals(1, state.entries.size)
        assertEquals(2L, state.entries.first().id)
    }

    @Test
    fun `deleteEntry forwards the id to the repository`() = runTest {
        val repository = FakeHistoryRepository()
        repository.historyFlow.value = listOf(sampleEntry(1L))
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.deleteEntry(1L)
        advanceUntilIdle()

        assertEquals(listOf(1L), repository.deletedIds)
        assertTrue(viewModel.uiState.value.entries.isEmpty())
    }

    @Test
    fun `clearHistory calls the repository and empties the state`() = runTest {
        val repository = FakeHistoryRepository()
        repository.historyFlow.value = listOf(sampleEntry(1L), sampleEntry(2L))
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.clearHistory()
        advanceUntilIdle()

        assertTrue(repository.clearCalled)
        assertTrue(viewModel.uiState.value.entries.isEmpty())
    }

    @Test
    fun `toggleFavorite forwards the id and new state to the repository`() = runTest {
        val repository = FakeHistoryRepository()
        val viewModel = HistoryViewModel(repository)
        advanceUntilIdle()

        viewModel.toggleFavorite(5L, true)
        advanceUntilIdle()

        assertEquals(listOf(5L to true), repository.toggledFavorites)
    }
}