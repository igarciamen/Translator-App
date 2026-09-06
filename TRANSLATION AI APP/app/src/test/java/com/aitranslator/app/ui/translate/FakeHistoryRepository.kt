package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.history.HistoryEntry
import com.aitranslator.app.domain.history.HistoryRepository
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeHistoryRepository : HistoryRepository {

    data class SavedCall(
        val sourceText: String,
        val translatedText: String,
        val sourceLanguage: Language,
        val targetLanguage: Language
    )

    val savedCalls = mutableListOf<SavedCall>()
    val toggledFavorites = mutableListOf<Pair<Long, Boolean>>()
    private val _history = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val historyFlow: StateFlow<List<HistoryEntry>> = _history
    private val _favorites = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val favoritesFlow: StateFlow<List<HistoryEntry>> = _favorites

    override suspend fun saveTranslation(
        sourceText: String,
        translatedText: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ) {
        savedCalls.add(SavedCall(sourceText, translatedText, sourceLanguage, targetLanguage))
    }

    override fun observeHistory() = historyFlow

    override fun observeFavorites() = favoritesFlow

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        toggledFavorites.add(id to isFavorite)
    }

    override suspend fun deleteEntry(id: Long) {
        _history.value = _history.value.filterNot { it.id == id }
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
    }
}