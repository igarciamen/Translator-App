package com.aitranslator.app.ui.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryEntry
import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.dictionary.DictionarySourceType
import com.aitranslator.app.domain.translation.Language

data class DictionaryUiState(
    val query: String = "",
    val language: Language = Language.SPANISH,
    val selectedSource: DictionarySourceType = DictionarySourceType.ONLINE,
    val isSearching: Boolean = false,
    val entry: DictionaryEntry? = null,
    val errorMessage: String? = null,
    val errorType: DictionaryErrorType? = null
)