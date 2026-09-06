package com.aitranslator.app.ui.ocr

import android.net.Uri
import com.aitranslator.app.domain.translation.Language

data class OcrUiState(
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val isExtracting: Boolean = false,
    val extractionError: String? = null,
    val sourceLanguage: Language = Language.SPANISH,
    val targetLanguage: Language = Language.ENGLISH,
    val translatedText: String = "",
    val isTranslating: Boolean = false,
    val translationError: String? = null,
    val cameraPermissionDenied: Boolean = false
)