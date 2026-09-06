package com.aitranslator.app.domain.phrases

import androidx.annotation.StringRes
import com.aitranslator.app.R

enum class PhraseCategory(@StringRes val labelRes: Int) {
    GREETINGS(R.string.phrase_category_greetings),
    RESTAURANT(R.string.phrase_category_restaurant),
    TRAVEL(R.string.phrase_category_travel),
    SHOPPING(R.string.phrase_category_shopping),
    EMERGENCY(R.string.phrase_category_emergency)
}