package com.aitranslator.app.domain.settings

import androidx.annotation.StringRes
import com.aitranslator.app.R

enum class AppLanguage(val localeTag: String?, @StringRes val labelRes: Int) {
    SYSTEM_DEFAULT(null, R.string.settings_language_system_default),
    ENGLISH("en", R.string.settings_language_english),
    SPANISH("es", R.string.settings_language_spanish)
}