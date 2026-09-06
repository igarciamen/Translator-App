package com.aitranslator.app.domain.speech

import com.aitranslator.app.domain.translation.Language

interface SpeechToTextEngine {
    suspend fun listen(language: Language): Result<String>
}