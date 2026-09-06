package com.aitranslator.app.domain.history

import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun saveTranslation(
        sourceText: String,
        translatedText: String,
        sourceLanguage: Language,
        targetLanguage: Language
    )

    fun observeHistory(): Flow<List<HistoryEntry>>

    fun observeFavorites(): Flow<List<HistoryEntry>>

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    suspend fun deleteEntry(id: Long)

    suspend fun clearHistory()
}