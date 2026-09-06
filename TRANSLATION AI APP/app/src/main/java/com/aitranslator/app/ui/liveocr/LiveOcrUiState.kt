package com.aitranslator.app.ui.liveocr

import com.aitranslator.app.domain.translation.Language

data class LiveOcrUiState(
    val detectedBlocks: List<DetectedTextBlock> = emptyList(),
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val rotationDegrees: Int = 0,
    val sourceLanguage: Language = Language.SPANISH,
    val targetLanguage: Language = Language.ENGLISH,
    val translations: Map<String, String> = emptyMap(),
    val isFrozen: Boolean = false
)