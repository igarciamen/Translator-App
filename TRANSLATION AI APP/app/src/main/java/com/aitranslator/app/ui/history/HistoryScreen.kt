package com.aitranslator.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitranslator.app.R
import com.aitranslator.app.domain.history.HistoryEntry

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onEntryClick: (HistoryEntry) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryScreenContent(
        uiState = uiState,
        onEntryClick = onEntryClick,
        onDeleteClick = viewModel::deleteEntry,
        onFavoriteToggle = viewModel::toggleFavorite,
        onShowFavoritesOnlyToggle = viewModel::onShowFavoritesOnlyToggled,
        modifier = modifier
    )
}

@Composable
private fun HistoryScreenContent(
    uiState: HistoryUiState,
    onEntryClick: (HistoryEntry) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onShowFavoritesOnlyToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        FavoritesOnlyRow(
            enabled = uiState.showFavoritesOnly,
            onToggle = onShowFavoritesOnlyToggle
        )

        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.entries.isEmpty() -> {
                EmptyHistoryPlaceholder(showFavoritesOnly = uiState.showFavoritesOnly)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.entries, key = { it.id }) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            onClick = { onEntryClick(entry) },
                            onDeleteClick = { onDeleteClick(entry.id) },
                            onFavoriteClick = { onFavoriteToggle(entry.id, !entry.isFavorite) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesOnlyRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.history_show_favorites_only),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun EmptyHistoryPlaceholder(
    showFavoritesOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (showFavoritesOnly) Icons.Filled.Star else Icons.Filled.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = stringResource(
                if (showFavoritesOnly) R.string.history_empty_favorites_title
                else R.string.history_empty_title
            ),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(
                if (showFavoritesOnly) R.string.history_empty_favorites_subtitle
                else R.string.history_empty_subtitle
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun HistoryEntryCard(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(entry.sourceLanguage.displayNameRes) +
                            " → " +
                            stringResource(entry.targetLanguage.displayNameRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = entry.sourceText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = entry.translatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (entry.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = stringResource(R.string.history_favorite_action),
                    tint = if (entry.isFavorite) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.history_delete_action),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}