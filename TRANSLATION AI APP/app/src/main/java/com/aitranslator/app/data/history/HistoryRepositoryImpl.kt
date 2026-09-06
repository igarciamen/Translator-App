package com.aitranslator.app.data.history

import com.aitranslator.app.domain.history.HistoryEntry
import com.aitranslator.app.domain.history.HistoryRepository
import com.aitranslator.app.domain.translation.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val dao: TranslationHistoryDao
) : HistoryRepository {

    override suspend fun saveTranslation(
        sourceText: String,
        translatedText: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ) {
        if (sourceText.isBlank() || translatedText.isBlank()) return

        dao.insert(
            TranslationHistoryEntity(
                sourceText = sourceText,
                translatedText = translatedText,
                sourceLanguageCode = sourceLanguage.isoCode,
                targetLanguageCode = targetLanguage.isoCode,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }

    override fun observeHistory(): Flow<List<HistoryEntry>> {
        return dao.observeAll().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override fun observeFavorites(): Flow<List<HistoryEntry>> {
        return dao.observeFavorites().map { entities ->
            entities.mapNotNull { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    override suspend fun deleteEntry(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun clearHistory() {
        dao.deleteAll()
    }
}