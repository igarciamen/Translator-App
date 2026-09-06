package com.aitranslator.app.data.dictionary

import com.aitranslator.app.domain.dictionary.DictionaryErrorType

class DictionaryLookupException(
    message: String,
    val errorType: DictionaryErrorType
) : Exception(message)