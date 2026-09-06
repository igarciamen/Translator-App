package com.aitranslator.app.di

import com.aitranslator.app.data.speech.AndroidSpeechToTextEngine
import com.aitranslator.app.data.speech.AndroidTextToSpeechEngine
import com.aitranslator.app.data.speech.SpeechToTextRepositoryImpl
import com.aitranslator.app.data.speech.TextToSpeechRepositoryImpl
import com.aitranslator.app.domain.speech.SpeechToTextEngine
import com.aitranslator.app.domain.speech.SpeechToTextRepository
import com.aitranslator.app.domain.speech.TextToSpeechEngine
import com.aitranslator.app.domain.speech.TextToSpeechRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {

    @Binds
    abstract fun bindTextToSpeechEngine(impl: AndroidTextToSpeechEngine): TextToSpeechEngine

    @Binds
    abstract fun bindTextToSpeechRepository(impl: TextToSpeechRepositoryImpl): TextToSpeechRepository

    @Binds
    abstract fun bindSpeechToTextEngine(impl: AndroidSpeechToTextEngine): SpeechToTextEngine

    @Binds
    abstract fun bindSpeechToTextRepository(impl: SpeechToTextRepositoryImpl): SpeechToTextRepository
}