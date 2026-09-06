package com.aitranslator.app.ui.translate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.domain.history.HistoryRepository
import com.aitranslator.app.domain.speech.SpeechToTextRepository
import com.aitranslator.app.domain.speech.TextToSpeechRepository
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.LanguageDetectionRepository
import com.aitranslator.app.domain.translation.TranslationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TranslateViewModel @Inject constructor(
    private val repository: TranslationRepository,
    private val languageDetectionRepository: LanguageDetectionRepository,
    private val historyRepository: HistoryRepository,
    private val textToSpeechRepository: TextToSpeechRepository,
    private val speechToTextRepository: SpeechToTextRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TranslateUiState())
    val uiState: StateFlow<TranslateUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { refreshDownloadStatus() }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun onSourceLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.targetLanguage) {
                current.copy(
                    sourceLanguage = language,
                    targetLanguage = current.sourceLanguage,
                    translatedText = "",
                    errorMessage = null
                )
            } else {
                current.copy(sourceLanguage = language, translatedText = "", errorMessage = null)
            }
        }
        viewModelScope.launch { refreshDownloadStatus() }
    }

    fun onTargetLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.sourceLanguage) {
                current.copy(
                    targetLanguage = language,
                    sourceLanguage = current.targetLanguage,
                    translatedText = "",
                    errorMessage = null
                )
            } else {
                current.copy(targetLanguage = language, translatedText = "", errorMessage = null)
            }
        }
        viewModelScope.launch { refreshDownloadStatus() }
    }

    fun swapLanguages() {
        _uiState.update { current ->
            current.copy(
                sourceLanguage = current.targetLanguage,
                targetLanguage = current.sourceLanguage,
                translatedText = "",
                errorMessage = null
            )
        }
        viewModelScope.launch { refreshDownloadStatus() }
    }

    fun onWifiOnlyToggled(enabled: Boolean) {
        _uiState.update { it.copy(downloadWifiOnly = enabled) }
    }

    fun onAutoDetectToggled(enabled: Boolean) {
        _uiState.update { it.copy(isAutoDetectEnabled = enabled, detectionError = null) }
    }

    fun downloadMissingModels() {
        val state = _uiState.value
        val languagesToDownload = buildList {
            if (!state.sourceLanguageDownloaded) add(state.sourceLanguage)
            if (!state.targetLanguageDownloaded) add(state.targetLanguage)
        }.distinct()

        if (languagesToDownload.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingModel = true, downloadError = null) }

            var failureMessage: String? = null
            languagesToDownload.forEach { language ->
                repository.downloadModel(language, requireWifi = _uiState.value.downloadWifiOnly)
                    .onFailure { failureMessage = it.message ?: "Download failed" }
            }

            refreshDownloadStatus()
            _uiState.update { it.copy(isDownloadingModel = false, downloadError = failureMessage) }
        }
    }

    fun translate() {
        val state = _uiState.value
        if (state.inputText.isBlank()) return

        viewModelScope.launch {
            if (state.isAutoDetectEnabled) {
                _uiState.update {
                    it.copy(isDetectingLanguage = true, detectionError = null, errorMessage = null)
                }

                languageDetectionRepository.detectLanguage(state.inputText).fold(
                    onSuccess = { detectedLanguage ->
                        _uiState.update {
                            it.copy(isDetectingLanguage = false, sourceLanguage = detectedLanguage)
                        }
                        refreshDownloadStatus()
                        performTranslation()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isDetectingLanguage = false,
                                detectionError = error.message ?: "Could not detect the language"
                            )
                        }
                    }
                )
            } else {
                performTranslation()
            }
        }
    }

    fun loadFromHistory(
        sourceText: String,
        translatedText: String,
        sourceLanguage: Language,
        targetLanguage: Language
    ) {
        _uiState.update {
            it.copy(
                inputText = sourceText,
                translatedText = translatedText,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage,
                errorMessage = null,
                detectionError = null
            )
        }
        viewModelScope.launch { refreshDownloadStatus() }
    }

    fun speakTranslation() {
        val state = _uiState.value
        if (state.translatedText.isBlank() || state.isSpeaking) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSpeaking = true, speechError = null) }

            textToSpeechRepository.speak(state.translatedText, state.targetLanguage).fold(
                onSuccess = {
                    _uiState.update { it.copy(isSpeaking = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSpeaking = false,
                            speechError = error.message ?: "Could not play the translation"
                        )
                    }
                }
            )
        }
    }

    fun stopSpeaking() {
        textToSpeechRepository.stop()
        _uiState.update { it.copy(isSpeaking = false) }
    }

    fun startListening() {
        if (_uiState.value.isListening) return

        viewModelScope.launch {
            _uiState.update { it.copy(isListening = true, listeningError = null) }

            speechToTextRepository.listen(_uiState.value.sourceLanguage).fold(
                onSuccess = { recognizedText ->
                    _uiState.update { it.copy(isListening = false, inputText = recognizedText) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isListening = false,
                            listeningError = error.message ?: "Could not recognize speech"
                        )
                    }
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeechRepository.stop()
    }

    private suspend fun performTranslation() {
        val state = _uiState.value
        _uiState.update { it.copy(isTranslating = true, errorMessage = null) }

        repository.translate(state.inputText, state.sourceLanguage, state.targetLanguage).fold(
            onSuccess = { translated ->
                _uiState.update { it.copy(isTranslating = false, translatedText = translated) }
                refreshDownloadStatus()
                historyRepository.saveTranslation(
                    sourceText = state.inputText,
                    translatedText = translated,
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage
                )
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        errorMessage = error.message ?: "Translation failed"
                    )
                }
            }
        )
    }

    private suspend fun refreshDownloadStatus() {
        val state = _uiState.value
        val sourceDownloaded = repository.isModelDownloaded(state.sourceLanguage)
        val targetDownloaded = repository.isModelDownloaded(state.targetLanguage)
        _uiState.update {
            it.copy(
                sourceLanguageDownloaded = sourceDownloaded,
                targetLanguageDownloaded = targetDownloaded
            )
        }
    }
}