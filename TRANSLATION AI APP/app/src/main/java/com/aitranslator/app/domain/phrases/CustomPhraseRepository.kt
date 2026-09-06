package com.aitranslator.app.domain.phrases

import kotlinx.coroutines.flow.Flow

interface CustomPhraseRepository {
    fun observeByCategory(category: PhraseCategory): Flow<List<Phrase>>
    suspend fun addPhrase(category: PhraseCategory, englishText: String)
    suspend fun deletePhrase(phraseId: String)
}