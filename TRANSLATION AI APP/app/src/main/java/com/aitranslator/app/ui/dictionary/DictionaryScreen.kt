package com.aitranslator.app.ui.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitranslator.app.R
import com.aitranslator.app.domain.dictionary.DictionarySourceType
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.LanguageDropdown
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DictionaryScreenContent(
        uiState = uiState,
        onQueryChanged = viewModel::onQueryChanged,
        onLanguageSelected = viewModel::onLanguageSelected,
        onSourceSelected = viewModel::onSourceSelected,
        onSearchClick = viewModel::onSearchClick,
        modifier = modifier
    )
}

@Composable
private fun DictionaryScreenContent(
    uiState: DictionaryUiState,
    onQueryChanged: (String) -> Unit,
    onLanguageSelected: (Language) -> Unit,
    onSourceSelected: (DictionarySourceType) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LanguageDropdown(
            label = "",
            selected = uiState.language,
            options = Language.supported,
            onSelected = onLanguageSelected,
            modifier = Modifier.fillMaxWidth()
        )

        SourceSelector(
            selectedSource = uiState.selectedSource,
            onSourceSelected = onSourceSelected
        )

        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChanged,
            label = { Text(stringResource(R.string.dictionary_search_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = onSearchClick,
            enabled = uiState.query.isNotBlank() && !uiState.isSearching,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(2.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.dictionary_search_button))
            }
        }

        when {
            uiState.errorMessage != null -> {
                ErrorCard(
                    message = uiState.errorMessage,
                    errorType = uiState.errorType,
                    onRetryClick = onSearchClick
                )
            }
            uiState.entry != null -> {
                ResultCard(entry = uiState.entry)
            }
            else -> {
                Text(
                    text = stringResource(R.string.dictionary_empty_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultCard(entry: com.aitranslator.app.domain.dictionary.DictionaryEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.word,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            entry.pronunciation?.let { pronunciation ->
                Text(
                    text = "/$pronunciation/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            entry.definitions.forEachIndexed { index, definition ->
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "${index + 1}. ${definition.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (definition.synonyms.isNotEmpty()) {
                        Text(
                            text = stringResource(
                                R.string.dictionary_synonyms_label,
                                definition.synonyms.joinToString(", ")
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    definition.example?.let { example ->
                        Text(
                            text = "\u201c$example\u201d",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    errorType: com.aitranslator.app.domain.dictionary.DictionaryErrorType?,
    onRetryClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )

            if (errorType == com.aitranslator.app.domain.dictionary.DictionaryErrorType.NETWORK_ERROR) {
                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.dictionary_retry_button))
                }
            }
        }
    }
}

@Composable
private fun SourceSelector(
    selectedSource: DictionarySourceType,
    onSourceSelected: (DictionarySourceType) -> Unit
) {
    val options = listOf(
        DictionarySourceType.ONLINE to stringResource(R.string.dictionary_source_online),
        DictionarySourceType.OFFLINE to stringResource(R.string.dictionary_source_offline)
    )

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (source, label) ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onClick = { onSourceSelected(source) },
                selected = selectedSource == source
            ) {
                Text(label)
            }
        }
    }
}