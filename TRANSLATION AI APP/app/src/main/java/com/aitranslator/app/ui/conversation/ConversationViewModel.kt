package com.aitranslator.app.ui.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.domain.speech.SpeechToTextRepository
import com.aitranslator.app.domain.speech.TextToSpeechRepository
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val speechToTextRepository: SpeechToTextRepository,
    private val translationRepository: TranslationRepository,
    private val textToSpeechRepository: TextToSpeechRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    fun onLanguageASelected(language: Language) {
        _uiState.update { current ->
            if (language == current.languageB) {
                current.copy(languageA = language, languageB = current.languageA)
            } else {
                current.copy(languageA = language)
            }
        }
    }

    fun onLanguageBSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.languageA) {
                current.copy(languageB = language, languageA = current.languageB)
            } else {
                current.copy(languageB = language)
            }
        }
    }

    fun onSwapSpeakers() {
        _uiState.update { current ->
            current.copy(languageA = current.languageB, languageB = current.languageA)
        }
    }

    fun onMicATapped() {
        val state = _uiState.value
        if (state.isListeningA || state.isListeningB) return

        _uiState.update { it.copy(isListeningA = true, errorA = null) }

        viewModelScope.launch {
            speechToTextRepository.listen(state.languageA).fold(
                onSuccess = { recognizedText -> translateAndPublish(recognizedText, spokenBySideA = true) },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isListeningA = false, errorA = error.message ?: "Could not recognize speech")
                    }
                }
            )
        }
    }

    fun onMicBTapped() {
        val state = _uiState.value
        if (state.isListeningA || state.isListeningB) return

        _uiState.update { it.copy(isListeningB = true, errorB = null) }

        viewModelScope.launch {
            speechToTextRepository.listen(state.languageB).fold(
                onSuccess = { recognizedText -> translateAndPublish(recognizedText, spokenBySideA = false) },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isListeningB = false, errorB = error.message ?: "Could not recognize speech")
                    }
                }
            )
        }
    }

    fun stopSpeaking() {
        textToSpeechRepository.stop()
        _uiState.update { it.copy(isSpeakingA = false, isSpeakingB = false) }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechRepository.stop()
    }

    private suspend fun translateAndPublish(recognizedText: String, spokenBySideA: Boolean) {
        val state = _uiState.value
        val sourceLanguage = if (spokenBySideA) state.languageA else state.languageB
        val targetLanguage = if (spokenBySideA) state.languageB else state.languageA

        translationRepository.translate(recognizedText, sourceLanguage, targetLanguage).fold(
            onSuccess = { translated ->
                _uiState.update {
                    it.copy(
                        isListeningA = false,
                        isListeningB = false,
                        lastMessage = ConversationMessage(
                            spokenBySideA = spokenBySideA,
                            originalText = recognizedText,
                            translatedText = translated
                        )
                    )
                }
                speakTranslation(translated, targetLanguage, listeningSideIsA = !spokenBySideA)
            },
            onFailure = { error ->
                _uiState.update {
                    if (spokenBySideA) {
                        it.copy(isListeningA = false, errorA = error.message ?: "Translation failed")
                    } else {
                        it.copy(isListeningB = false, errorB = error.message ?: "Translation failed")
                    }
                }
            }
        )
    }

    private suspend fun speakTranslation(text: String, language: Language, listeningSideIsA: Boolean) {
        _uiState.update {
            if (listeningSideIsA) it.copy(isSpeakingA = true) else it.copy(isSpeakingB = true)
        }

        textToSpeechRepository.speak(text, language)

        _uiState.update {
            if (listeningSideIsA) it.copy(isSpeakingA = false) else it.copy(isSpeakingB = false)
        }
    }
}