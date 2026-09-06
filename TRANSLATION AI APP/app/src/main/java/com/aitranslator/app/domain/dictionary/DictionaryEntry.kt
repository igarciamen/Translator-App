package com.aitranslator.app.domain.dictionary

data class DictionaryEntry(
    val word: String,
    val pronunciation: String? = null,
    val definitions: List<DictionaryDefinition>
)