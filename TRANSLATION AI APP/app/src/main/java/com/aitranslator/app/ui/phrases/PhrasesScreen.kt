package com.aitranslator.app.ui.phrases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitranslator.app.R
import com.aitranslator.app.domain.phrases.Phrase
import com.aitranslator.app.domain.phrases.PhraseCategory
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.LanguageDropdown

@Composable
fun PhrasesScreen(
    modifier: Modifier = Modifier,
    viewModel: PhrasesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopSpeaking()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PhrasesScreenContent(
        uiState = uiState,
        onCategorySelected = viewModel::onCategorySelected,
        onTargetLanguageSelected = viewModel::onTargetLanguageSelected,
        onSpeakClick = viewModel::onSpeakClick,
        onNewPhraseTextChanged = viewModel::onNewPhraseTextChanged,
        onAddPhraseClick = viewModel::onAddPhraseClick,
        onDeletePhraseClick = viewModel::onDeletePhraseClick,
        modifier = modifier
    )
}

@Composable
private fun PhrasesScreenContent(
    uiState: PhrasesUiState,
    onCategorySelected: (PhraseCategory) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onSpeakClick: (Phrase) -> Unit,
    onNewPhraseTextChanged: (String) -> Unit,
    onAddPhraseClick: () -> Unit,
    onDeletePhraseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        LanguageDropdown(
            label = stringResource(R.string.phrases_translate_to_label),
            selected = uiState.targetLanguage,
            options = Language.supported,
            onSelected = onTargetLanguageSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        CategoryRow(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = onCategorySelected
        )

        AddPhraseRow(
            text = uiState.newPhraseText,
            onTextChanged = onNewPhraseTextChanged,
            onAddClick = onAddPhraseClick
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.displayPhrases, key = { it.phrase.id }) { displayPhrase ->
                PhraseCard(
                    displayPhrase = displayPhrase,
                    translationState = uiState.translations[displayPhrase.phrase.id],
                    isSpeaking = uiState.speakingPhraseId == displayPhrase.phrase.id,
                    onSpeakClick = { onSpeakClick(displayPhrase.phrase) },
                    onDeleteClick = { onDeletePhraseClick(displayPhrase.phrase.id) }
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    selectedCategory: PhraseCategory,
    onCategorySelected: (PhraseCategory) -> Unit
) {
    // LazyRow instead of a plain Row: with 5 categories, a plain Row
    // overflows the screen width and silently clips whatever doesn't
    // fit — which was hiding the last category ("Emergency") entirely.
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PhraseCategory.entries) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(stringResource(category.labelRes)) }
            )
        }
    }
}

@Composable
private fun AddPhraseRow(
    text: String,
    onTextChanged: (String) -> Unit,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            label = { Text(stringResource(R.string.phrases_add_hint)) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.phrases_add_action),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PhraseCard(
    displayPhrase: DisplayPhrase,
    translationState: PhraseTranslationState?,
    isSpeaking: Boolean,
    onSpeakClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayPhrase.phrase.englishText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                when (translationState) {
                    is PhraseTranslationState.Loading -> {
                        Text(
                            text = stringResource(R.string.phrases_translating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is PhraseTranslationState.Success -> {
                        Text(
                            text = translationState.translatedText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    is PhraseTranslationState.Error -> {
                        Text(
                            text = translationState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    null -> Unit
                }
            }

            if (translationState is PhraseTranslationState.Success) {
                IconButton(onClick = onSpeakClick, enabled = !isSpeaking) {
                    if (isSpeaking) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = stringResource(R.string.translate_listen_action),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (displayPhrase.isCustom) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.phrases_delete_action),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}