package com.aitranslator.app.data.speech

import com.aitranslator.app.domain.speech.SpeechToTextEngine
import com.aitranslator.app.domain.speech.SpeechToTextRepository
import com.aitranslator.app.domain.translation.Language
import javax.inject.Inject

class SpeechToTextRepositoryImpl @Inject constructor(
    private val engine: SpeechToTextEngine
) : SpeechToTextRepository {

    override suspend fun listen(language: Language): Result<String> {
        return engine.listen(language)
    }
}