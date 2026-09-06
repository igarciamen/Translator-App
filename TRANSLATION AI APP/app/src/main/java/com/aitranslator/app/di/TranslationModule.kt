package com.aitranslator.app.di

import com.aitranslator.app.data.translation.LanguageDetectionRepositoryImpl
import com.aitranslator.app.data.translation.MlKitLanguageDetector
import com.aitranslator.app.data.translation.MlKitTranslationEngine
import com.aitranslator.app.data.translation.TranslationRepositoryImpl
import com.aitranslator.app.domain.translation.LanguageDetectionRepository
import com.aitranslator.app.domain.translation.LanguageDetector
import com.aitranslator.app.domain.translation.TranslationEngine
import com.aitranslator.app.domain.translation.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationModule {

    @Binds
    abstract fun bindTranslationEngine(impl: MlKitTranslationEngine): TranslationEngine

    @Binds
    abstract fun bindTranslationRepository(impl: TranslationRepositoryImpl): TranslationRepository

    @Binds
    abstract fun bindLanguageDetector(impl: MlKitLanguageDetector): LanguageDetector

    @Binds
    abstract fun bindLanguageDetectionRepository(impl: LanguageDetectionRepositoryImpl): LanguageDetectionRepository
}