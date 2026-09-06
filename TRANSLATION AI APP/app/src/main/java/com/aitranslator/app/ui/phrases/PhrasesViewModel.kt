package com.aitranslator.app.ui.phrases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.domain.phrases.CustomPhraseRepository
import com.aitranslator.app.domain.phrases.Phrase
import com.aitranslator.app.domain.phrases.PhraseCatalog
import com.aitranslator.app.domain.phrases.PhraseCategory
import com.aitranslator.app.domain.speech.TextToSpeechRepository
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhrasesViewModel @Inject constructor(
    private val translationRepository: TranslationRepository,
    private val textToSpeechRepository: TextToSpeechRepository,
    private val customPhraseRepository: CustomPhraseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhrasesUiState())
    val uiState: StateFlow<PhrasesUiState> = _uiState.asStateFlow()

    private var customPhrasesJob: Job? = null

    init {
        observeCategory(PhraseCategory.GREETINGS)
    }

    fun onCategorySelected(category: PhraseCategory) {
        _uiState.update {
            it.copy(selectedCategory = category, displayPhrases = emptyList(), translations = emptyMap())
        }
        observeCategory(category)
    }

    fun onTargetLanguageSelected(language: Language) {
        _uiState.update { it.copy(targetLanguage = language, translations = emptyMap()) }
        translatePhrases(_uiState.value.displayPhrases.map { it.phrase })
    }

    fun onNewPhraseTextChanged(text: String) {
        _uiState.update { it.copy(newPhraseText = text) }
    }

    fun onAddPhraseClick() {
        val text = _uiState.value.newPhraseText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            customPhraseRepository.addPhrase(_uiState.value.selectedCategory, text)
            _uiState.update { it.copy(newPhraseText = "") }
        }
    }

    fun onDeletePhraseClick(phraseId: String) {
        viewModelScope.launch {
            customPhraseRepository.deletePhrase(phraseId)
        }
    }

    fun onSpeakClick(phrase: Phrase) {
        val state = _uiState.value
        if (state.speakingPhraseId != null) return

        val translation = state.translations[phrase.id] as? PhraseTranslationState.Success ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(speakingPhraseId = phrase.id) }
            textToSpeechRepository.speak(translation.translatedText, state.targetLanguage)
            _uiState.update { it.copy(speakingPhraseId = null) }
        }
    }

    fun stopSpeaking() {
        textToSpeechRepository.stop()
        _uiState.update { it.copy(speakingPhraseId = null) }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechRepository.stop()
    }

    /**
     * Restarts the Room observation for the given category — cancelling
     * any previous subscription first, so switching categories quickly
     * doesn't leave a stale collector from the old category still
     * updating the state in the background.
     */
    private fun observeCategory(category: PhraseCategory) {
        customPhrasesJob?.cancel()
        customPhrasesJob = viewModelScope.launch {
            customPhraseRepository.observeByCategory(category).collect { customPhrases ->
                val builtIn = PhraseCatalog.byCategory(category).map { DisplayPhrase(it, isCustom = false) }
                val custom = customPhrases.map { DisplayPhrase(it, isCustom = true) }
                val displayPhrases = builtIn + custom

                _uiState.update { it.copy(displayPhrases = displayPhrases) }
                translatePhrases(displayPhrases.map { it.phrase })
            }
        }
    }

    private fun translatePhrases(phrases: List<Phrase>) {
        val language = _uiState.value.targetLanguage

        _uiState.update { current ->
            current.copy(
                translations = current.translations + phrases
                    .filterNot { current.translations.containsKey(it.id) }
                    .associate { it.id to PhraseTranslationState.Loading }
            )
        }

        phrases.forEach { phrase ->
            viewModelScope.launch {
                translationRepository.translate(phrase.englishText, Language.ENGLISH, language).fold(
                    onSuccess = { translated ->
                        _uiState.update {
                            it.copy(translations = it.translations + (phrase.id to PhraseTranslationState.Success(translated)))
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(translations = it.translations + (phrase.id to PhraseTranslationState.Error(error.message ?: "Translation failed")))
                        }
                    }
                )
            }
        }
    }
}