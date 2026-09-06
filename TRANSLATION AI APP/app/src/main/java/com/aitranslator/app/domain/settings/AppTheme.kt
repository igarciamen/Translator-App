package com.aitranslator.app.domain.settings

import androidx.annotation.StringRes
import com.aitranslator.app.R

enum class AppTheme(@StringRes val labelRes: Int) {
    SYSTEM_DEFAULT(R.string.settings_theme_system_default),
    LIGHT(R.string.settings_theme_light),
    DARK(R.string.settings_theme_dark)
}