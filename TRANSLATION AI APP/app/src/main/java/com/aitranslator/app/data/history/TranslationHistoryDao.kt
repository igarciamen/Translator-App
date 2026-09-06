package com.aitranslator.app.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {

    @Insert
    suspend fun insert(entity: TranslationHistoryEntity): Long

    @Query("SELECT * FROM translation_history ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<TranslationHistoryEntity>>

    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestampMillis DESC")
    fun observeFavorites(): Flow<List<TranslationHistoryEntity>>

    @Query("UPDATE translation_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM translation_history")
    suspend fun deleteAll()
}