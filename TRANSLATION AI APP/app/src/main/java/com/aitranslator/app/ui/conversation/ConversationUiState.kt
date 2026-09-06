package com.aitranslator.app.ui.conversation

import com.aitranslator.app.domain.translation.Language

data class ConversationUiState(
    val languageA: Language = Language.SPANISH,
    val languageB: Language = Language.ENGLISH,
    val isListeningA: Boolean = false,
    val isListeningB: Boolean = false,
    val lastMessage: ConversationMessage? = null,
    val errorA: String? = null,
    val errorB: String? = null,
    val isSpeakingA: Boolean = false,
    val isSpeakingB: Boolean = false
)