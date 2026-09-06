package com.aitranslator.app.data.phrases

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_phrases")
data class CustomPhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String,
    val englishText: String
)