package com.aitranslator.app.domain.phrases

data class Phrase(
    val id: String,
    val category: PhraseCategory,
    val englishText: String
)