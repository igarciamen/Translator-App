package com.aitranslator.app.ui.history

import com.aitranslator.app.domain.history.HistoryEntry

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val showFavoritesOnly: Boolean = false
)