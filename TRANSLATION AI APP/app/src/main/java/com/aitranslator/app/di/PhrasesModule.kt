package com.aitranslator.app.di

import com.aitranslator.app.data.history.AppDatabase
import com.aitranslator.app.data.phrases.CustomPhraseDao
import com.aitranslator.app.data.phrases.CustomPhraseRepositoryImpl
import com.aitranslator.app.domain.phrases.CustomPhraseRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PhrasesModule {

    @Binds
    abstract fun bindCustomPhraseRepository(impl: CustomPhraseRepositoryImpl): CustomPhraseRepository

    companion object {
        @Provides
        fun provideCustomPhraseDao(database: AppDatabase): CustomPhraseDao {
            return database.customPhraseDao()
        }
    }
}