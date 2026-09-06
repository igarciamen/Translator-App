package com.aitranslator.app.ui.translate

import com.aitranslator.app.domain.speech.SpeechToTextRepository
import com.aitranslator.app.domain.translation.Language

class FakeSpeechToTextRepository : SpeechToTextRepository {
    var lastLanguage: Language? = null
    var result: Result<String> = Result.success("")

    override suspend fun listen(language: Language): Result<String> {
        lastLanguage = language
        return result
    }
}