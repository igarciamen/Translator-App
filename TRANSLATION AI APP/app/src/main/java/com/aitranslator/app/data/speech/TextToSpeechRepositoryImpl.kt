package com.aitranslator.app.data.speech

import com.aitranslator.app.domain.speech.TextToSpeechEngine
import com.aitranslator.app.domain.speech.TextToSpeechRepository
import com.aitranslator.app.domain.translation.Language
import javax.inject.Inject

class TextToSpeechRepositoryImpl @Inject constructor(
    private val engine: TextToSpeechEngine
) : TextToSpeechRepository {

    override suspend fun speak(text: String, language: Language): Result<Unit> {
        if (text.isBlank()) return Result.success(Unit)
        return engine.speak(text, language)
    }

    override fun stop() {
        engine.stop()
    }
}