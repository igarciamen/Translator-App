package com.aitranslator.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.domain.history.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val showFavoritesOnly = MutableStateFlow(false)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.observeHistory(),
        repository.observeFavorites(),
        showFavoritesOnly
    ) { history, favorites, favoritesOnly ->
        HistoryUiState(
            entries = if (favoritesOnly) favorites else history,
            isLoading = false,
            showFavoritesOnly = favoritesOnly
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HistoryUiState()
    )

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun toggleFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFavorite)
        }
    }

    fun onShowFavoritesOnlyToggled(enabled: Boolean) {
        showFavoritesOnly.value = enabled
    }
}