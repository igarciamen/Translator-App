package com.aitranslator.app.di

import com.aitranslator.app.data.dictionary.OfflineDictionaryRepository
import com.aitranslator.app.data.dictionary.WiktionaryDictionaryRepository
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DictionaryModule {

    @Binds
    @OnlineDictionary
    abstract fun bindOnlineDictionaryRepository(impl: WiktionaryDictionaryRepository): DictionaryRepository

    @Binds
    @OfflineDictionary
    abstract fun bindOfflineDictionaryRepository(impl: OfflineDictionaryRepository): DictionaryRepository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient()
    }
}