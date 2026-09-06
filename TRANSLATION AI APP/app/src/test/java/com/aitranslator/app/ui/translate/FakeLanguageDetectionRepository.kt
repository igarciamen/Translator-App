package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.domain.translation.LanguageDetectionRepository

class FakeLanguageDetectionRepository : LanguageDetectionRepository {

    var lastInput: String? = null
    var result: Result<Language> = Result.success(Language.ENGLISH)

    override suspend fun detectLanguage(text: String): Result<Language> {
        lastInput = text
        return result
    }
}