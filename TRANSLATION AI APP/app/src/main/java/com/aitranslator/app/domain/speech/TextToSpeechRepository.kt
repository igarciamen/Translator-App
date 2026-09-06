package com.aitranslator.app.domain.speech

import com.aitranslator.app.domain.translation.Language

interface TextToSpeechRepository {
    suspend fun speak(text: String, language: Language): Result<Unit>
    fun stop()
}