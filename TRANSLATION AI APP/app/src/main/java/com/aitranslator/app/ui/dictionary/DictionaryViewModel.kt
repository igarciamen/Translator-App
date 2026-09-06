package com.aitranslator.app.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aitranslator.app.data.dictionary.DictionaryLookupException
import com.aitranslator.app.di.OfflineDictionary
import com.aitranslator.app.di.OnlineDictionary
import com.aitranslator.app.domain.dictionary.DictionaryErrorType
import com.aitranslator.app.domain.dictionary.DictionaryRepository
import com.aitranslator.app.domain.dictionary.DictionarySourceType
import com.aitranslator.app.domain.translation.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    @OnlineDictionary private val onlineRepository: DictionaryRepository,
    @OfflineDictionary private val offlineRepository: DictionaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query, entry = null, errorMessage = null, errorType = null) }
    }

    fun onLanguageSelected(language: Language) {
        _uiState.update { it.copy(language = language, entry = null, errorMessage = null, errorType = null) }
    }

    fun onSourceSelected(source: DictionarySourceType) {
        _uiState.update { it.copy(selectedSource = source, entry = null, errorMessage = null, errorType = null) }
    }

    fun onSearchClick() {
        val state = _uiState.value
        if (state.query.isBlank() || state.isSearching) return

        val repository = if (state.selectedSource == DictionarySourceType.OFFLINE) {
            offlineRepository
        } else {
            onlineRepository
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, errorMessage = null, errorType = null, entry = null) }

            repository.lookup(state.query, state.language).fold(
                onSuccess = { entry ->
                    _uiState.update { it.copy(isSearching = false, entry = entry) }
                },
                onFailure = { error ->
                    val errorType = (error as? DictionaryLookupException)?.errorType ?: DictionaryErrorType.UNKNOWN
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            errorMessage = error.message ?: "Lookup failed",
                            errorType = errorType
                        )
                    }
                }
            )
        }
    }

    fun onRetryClick() {
        val state = _uiState.value
        if (state.query.isBlank() || state.isSearching) return
        onSearchClick()
    }
}