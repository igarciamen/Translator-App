package com.aitranslator.app.domain.translation

import androidx.annotation.StringRes
import com.aitranslator.app.R

enum class Language(val isoCode: String, @StringRes val displayNameRes: Int) {
    SPANISH("es", R.string.language_spanish),
    ENGLISH("en", R.string.language_english),
    FRENCH("fr", R.string.language_french),
    GERMAN("de", R.string.language_german),
    ITALIAN("it", R.string.language_italian),
    PORTUGUESE("pt", R.string.language_portuguese),
    CATALAN("ca", R.string.language_catalan),
    CHINESE("zh", R.string.language_chinese),
    HINDI("hi", R.string.language_hindi),
    BENGALI("bn", R.string.language_bengali),
    TAMIL("ta", R.string.language_tamil),
    VIETNAMESE("vi", R.string.language_vietnamese),
    THAI("th", R.string.language_thai),
    INDONESIAN("id", R.string.language_indonesian),
    MALAY("ms", R.string.language_malay),
    TAGALOG("tl", R.string.language_tagalog);

    companion object {
        val supported: List<Language> = entries

        fun fromIsoCode(code: String): Language? {
            return supported.find { it.isoCode == code }
        }
    }
}