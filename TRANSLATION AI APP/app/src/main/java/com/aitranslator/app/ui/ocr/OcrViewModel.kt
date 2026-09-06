package com.aitranslator.app.ui.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.data.ocr.CameraCaptureUriProvider
import com.aitranslator.app.domain.ocr.OcrRepository
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
class OcrViewModel @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val translationRepository: TranslationRepository,
    private val cameraCaptureUriProvider: CameraCaptureUriProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun prepareCameraCaptureUri(): Uri {
        return cameraCaptureUriProvider.createImageUri()
    }

    fun onImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                imageUri = uri,
                extractedText = "",
                translatedText = "",
                extractionError = null,
                translationError = null
            )
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isExtracting = true) }

            ocrRepository.extractText(uri).fold(
                onSuccess = { text ->
                    _uiState.update { it.copy(isExtracting = false, extractedText = text) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isExtracting = false,
                            extractionError = error.message ?: "Could not read text from the image"
                        )
                    }
                }
            )
        }
    }

    fun onSourceLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.targetLanguage) {
                current.copy(
                    sourceLanguage = language,
                    targetLanguage = current.sourceLanguage,
                    translatedText = ""
                )
            } else {
                current.copy(sourceLanguage = language, translatedText = "")
            }
        }
    }

    fun onTargetLanguageSelected(language: Language) {
        _uiState.update { current ->
            if (language == current.sourceLanguage) {
                current.copy(
                    targetLanguage = language,
                    sourceLanguage = current.targetLanguage,
                    translatedText = ""
                )
            } else {
                current.copy(targetLanguage = language, translatedText = "")
            }
        }
    }

    fun translateExtractedText() {
        val state = _uiState.value
        if (state.extractedText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, translationError = null) }

            translationRepository.translate(
                state.extractedText,
                state.sourceLanguage,
                state.targetLanguage
            ).fold(
                onSuccess = { translated ->
                    _uiState.update { it.copy(isTranslating = false, translatedText = translated) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isTranslating = false,
                            translationError = error.message ?: "Translation failed"
                        )
                    }
                }
            )
        }
    }

    fun onCameraPermissionDenied() {
        _uiState.update { it.copy(cameraPermissionDenied = true) }
    }

    fun onCameraPermissionGranted() {
        _uiState.update { it.copy(cameraPermissionDenied = false) }
    }
}