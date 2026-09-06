package com.aitranslator.app.ui.conversation

import com.aitranslator.app.domain.speech.TextToSpeechRepository
import com.aitranslator.app.domain.translation.Language

class FakeTextToSpeechRepository : TextToSpeechRepository {
    var lastSpokenText: String? = null
    var lastSpokenLanguage: Language? = null
    var result: Result<Unit> = Result.success(Unit)
    var stopCallCount = 0

    override suspend fun speak(text: String, language: Language): Result<Unit> {
        lastSpokenText = text
        lastSpokenLanguage = language
        return result
    }

    override fun stop() {
        stopCallCount++
    }
}