package com.aitranslator.app.ui.conversation

data class ConversationMessage(
    val spokenBySideA: Boolean,
    val originalText: String,
    val translatedText: String
)