package com.aitranslator.app.data.history

import com.aitranslator.app.domain.history.HistoryEntry
import com.aitranslator.app.domain.translation.Language

fun TranslationHistoryEntity.toDomain(): HistoryEntry? {
    val source = Language.fromIsoCode(sourceLanguageCode) ?: return null
    val target = Language.fromIsoCode(targetLanguageCode) ?: return null
    return HistoryEntry(
        id = id,
        sourceText = sourceText,
        translatedText = translatedText,
        sourceLanguage = source,
        targetLanguage = target,
        timestampMillis = timestampMillis,
        isFavorite = isFavorite
    )
}