package com.aitranslator.app.ui.phrases

import com.aitranslator.app.domain.phrases.Phrase
import com.aitranslator.app.domain.phrases.PhraseCategory
import com.aitranslator.app.domain.translation.Language

sealed class PhraseTranslationState {
    object Loading : PhraseTranslationState()
    data class Success(val translatedText: String) : PhraseTranslationState()
    data class Error(val message: String) : PhraseTranslationState()
}

data class DisplayPhrase(
    val phrase: Phrase,
    val isCustom: Boolean
)

data class PhrasesUiState(
    val selectedCategory: PhraseCategory = PhraseCategory.GREETINGS,
    val targetLanguage: Language = Language.SPANISH,
    val displayPhrases: List<DisplayPhrase> = emptyList(),
    val translations: Map<String, PhraseTranslationState> = emptyMap(),
    val speakingPhraseId: String? = null,
    val newPhraseText: String = ""
)