package com.aitranslator.app.data.history

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aitranslator.app.data.phrases.CustomPhraseDao
import com.aitranslator.app.data.phrases.CustomPhraseEntity

@Database(
    entities = [TranslationHistoryEntity::class, CustomPhraseEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationHistoryDao(): TranslationHistoryDao
    abstract fun customPhraseDao(): CustomPhraseDao
}