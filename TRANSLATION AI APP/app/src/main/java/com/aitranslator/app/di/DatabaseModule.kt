package com.aitranslator.app.di

import android.content.Context
import androidx.room.Room
import com.aitranslator.app.data.history.AppDatabase
import com.aitranslator.app.data.history.HistoryRepositoryImpl
import com.aitranslator.app.data.history.TranslationHistoryDao
import com.aitranslator.app.domain.history.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseBindingsModule {
    @Binds
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseProvidersModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aitranslator.db"
        )
            // Development-time only: recreates the database on schema changes
            // instead of requiring a Migration. Must be replaced with a real
            // Migration before shipping to real users with existing data.
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTranslationHistoryDao(database: AppDatabase): TranslationHistoryDao {
        return database.translationHistoryDao()
    }
}