package com.aitranslator.app.data.phrases

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPhraseDao {

    @Insert
    suspend fun insert(entity: CustomPhraseEntity)

    @Query("SELECT * FROM custom_phrases WHERE categoryName = :categoryName ORDER BY id DESC")
    fun observeByCategory(categoryName: String): Flow<List<CustomPhraseEntity>>

    @Query("DELETE FROM custom_phrases WHERE id = :id")
    suspend fun deleteById(id: Long)
}