package com.aitranslator.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector
import com.aitranslator.app.R

sealed class Screen(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    object Translate : Screen("translate", R.string.nav_translate, Icons.Filled.Translate)
    object Conversation : Screen("conversation", R.string.nav_conversation, Icons.Filled.RecordVoiceOver)
    object Camera : Screen("camera", R.string.nav_camera, Icons.Filled.CameraAlt)
    object Dictionary : Screen("dictionary", R.string.nav_dictionary, Icons.Filled.MenuBook)
    object Phrases : Screen("phrases", R.string.nav_phrases, Icons.Filled.FormatQuote)
    object More : Screen("more", R.string.nav_more, Icons.Filled.MoreHoriz)

    // Not part of the bottom navigation bar: reached from an icon inside
    // the Translate screen, pushed on top of the back stack.
    object History : Screen("history", R.string.nav_history, Icons.Filled.History)

    // Not part of the bottom navigation bar either: reached from a button
    // inside the Camera (OCR) screen.
    object LiveOcr : Screen("live_ocr", R.string.nav_live_ocr, Icons.Filled.CameraAlt)

    companion object {
        val bottomNavItems: List<Screen> = listOf(
            Translate, Conversation, Camera, Dictionary, Phrases, More
        )
    }
}