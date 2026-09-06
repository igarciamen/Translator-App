package com.aitranslator.app.domain.dictionary

data class DictionaryDefinition(
    val text: String,
    val synonyms: List<String> = emptyList(),
    val example: String? = null
)