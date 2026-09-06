package com.aitranslator.app.data.translation

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.LanguageDetectionRepository
import com.aitranslator.app.domain.translation.LanguageDetector
import javax.inject.Inject

class LanguageDetectionRepositoryImpl @Inject constructor(
    private val detector: LanguageDetector
) : LanguageDetectionRepository {

    override suspend fun detectLanguage(text: String): Result<Language> {
        if (text.isBlank()) {
            return Result.failure(IllegalArgumentException("Cannot detect the language of empty text"))
        }
        return detector.detectLanguage(text)
    }
}