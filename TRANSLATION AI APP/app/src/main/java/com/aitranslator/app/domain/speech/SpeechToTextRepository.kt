package com.aitranslator.app.domain.speech

import com.aitranslator.app.domain.translation.Language

interface SpeechToTextRepository {
    suspend fun listen(language: Language): Result<String>
}