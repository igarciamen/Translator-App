package com.aitranslator.app.ui.phrases

import com.aitranslator.app.domain.phrases.CustomPhraseRepository
import com.aitranslator.app.domain.phrases.Phrase
import com.aitranslator.app.domain.phrases.PhraseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCustomPhraseRepository : CustomPhraseRepository {
    private var nextId = 1L
    private val phrasesByCategory = mutableMapOf<PhraseCategory, MutableStateFlow<List<Phrase>>>()

    private fun flowFor(category: PhraseCategory) =
        phrasesByCategory.getOrPut(category) { MutableStateFlow(emptyList()) }

    override fun observeByCategory(category: PhraseCategory) = flowFor(category).asStateFlow()

    override suspend fun addPhrase(category: PhraseCategory, englishText: String) {
        val flow = flowFor(category)
        val newPhrase = Phrase(id = "custom_${nextId++}", category = category, englishText = englishText)
        flow.value = flow.value + newPhrase
    }

    override suspend fun deletePhrase(phraseId: String) {
        phrasesByCategory.values.forEach { flow ->
            flow.value = flow.value.filterNot { it.id == phraseId }
        }
    }
}