package com.aitranslator.app.ui.liveocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LiveOcrViewModel @Inject constructor(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveOcrUiState())
    val uiState: StateFlow<LiveOcrUiState> = _uiState.asStateFlow()

    private var lastNonEmptyTimestamp = 0L
    private var lastDisplayUpdateTimestamp = 0L
    private var lastDisplayedNormalizedTexts: Set<String> = emptySet()

    private val emptyGracePeriodMillis = 1200L
    private val minDisplayDurationMillis = 3000L

    fun onTextBlocksDetected(
        blocks: List<DetectedTextBlock>,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int
    ) {
        if (_uiState.value.isFrozen) return

        val now = System.currentTimeMillis()
        val newNormalizedTexts = blocks.map { TextNormalizer.normalize(it.text) }.toSet()

        if (blocks.isNotEmpty()) {
            lastNonEmptyTimestamp = now

            val isFirstDetection = lastDisplayedNormalizedTexts.isEmpty()
            val minDurationElapsed = now - lastDisplayUpdateTimestamp >= minDisplayDurationMillis
            val isSubstantiallyDifferent = isSubstantiallyDifferent(
                lastDisplayedNormalizedTexts,
                newNormalizedTexts
            )

            val shouldUpdateDisplay = isFirstDetection || minDurationElapsed || isSubstantiallyDifferent

            if (shouldUpdateDisplay) {
                lastDisplayUpdateTimestamp = now
                lastDisplayedNormalizedTexts = newNormalizedTexts
                _uiState.update {
                    it.copy(
                        detectedBlocks = blocks,
                        imageWidth = imageWidth,
                        imageHeight = imageHeight,
                        rotationDegrees = rotationDegrees
                    )
                }
            }
        } else if (now - lastNonEmptyTimestamp > emptyGracePeriodMillis) {
            lastDisplayedNormalizedTexts = emptySet()
            _uiState.update { it.copy(detectedBlocks = emptyList()) }
        }

        val state = _uiState.value
        val untranslated = blocks
            .map { TextNormalizer.normalize(it.text) }
            .distinct()
            .filter { it.isNotBlank() }
            .filterNot { state.translations.containsKey(it) }

        if (untranslated.isEmpty()) return

        viewModelScope.launch {
            untranslated.forEach { normalizedText ->
                translationRepository.translate(
                    normalizedText,
                    _uiState.value.sourceLanguage,
                    _uiState.value.targetLanguage
                ).onSuccess { translated ->
                    _uiState.update {
                        it.copy(translations = it.translations + (normalizedText to translated))
                    }
                }
            }
        }
    }

    fun onScreenTapped() {
        _uiState.update { it.copy(isFrozen = !it.isFrozen) }
    }

    private fun isSubstantiallyDifferent(previous: Set<String>, current: Set<String>): Boolean {
        if (previous.isEmpty() || current.isEmpty()) return true

        val previousLongest = previous.maxByOrNull { it.length } ?: return true
        val currentLongest = current.maxByOrNull { it.length } ?: return true

        return !TextSimilarity.isSimilar(previousLongest, currentLongest)
    }

    fun onSourceLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.targetLanguage) {
                current.copy(
                    sourceLanguage = language,
                    targetLanguage = current.sourceLanguage,
                    translations = emptyMap()
                )
            } else {
                current.copy(sourceLanguage = language, translations = emptyMap())
            }
        }
    }

    fun onTargetLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.sourceLanguage) {
                current.copy(
                    targetLanguage = language,
                    sourceLanguage = current.targetLanguage,
                    translations = emptyMap()
                )
            } else {
                current.copy(targetLanguage = language, translations = emptyMap())
            }
        }
    }
}