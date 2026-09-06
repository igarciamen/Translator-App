package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.translation.Language

data class TranslateUiState(
    val inputText: String = "",
    val translatedText: String = "",
    val sourceLanguage: Language = Language.SPANISH,
    val targetLanguage: Language = Language.ENGLISH,
    val isTranslating: Boolean = false,
    val errorMessage: String? = null,
    val sourceLanguageDownloaded: Boolean = false,
    val targetLanguageDownloaded: Boolean = false,
    val isDownloadingModel: Boolean = false,
    val downloadWifiOnly: Boolean = true,
    val downloadError: String? = null,
    val isAutoDetectEnabled: Boolean = false,
    val isDetectingLanguage: Boolean = false,
    val detectionError: String? = null,
    val isSpeaking: Boolean = false,
    val speechError: String? = null,
    val isListening: Boolean = false,
    val listeningError: String? = null
)