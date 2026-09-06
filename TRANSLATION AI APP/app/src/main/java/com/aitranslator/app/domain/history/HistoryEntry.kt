package com.aitranslator.app.domain.history

import com.aitranslator.app.domain.translation.Language

data class HistoryEntry(
    val id: Long = 0L,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val timestampMillis: Long,
    val isFavorite: Boolean = false
)