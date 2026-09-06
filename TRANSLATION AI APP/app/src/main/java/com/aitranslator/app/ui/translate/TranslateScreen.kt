package com.aitranslator.app.ui.translate

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aitranslator.app.R
import com.aitranslator.app.domain.translation.Language

@Composable
fun TranslateScreen(
    modifier: Modifier = Modifier,
    onHistoryClick: () -> Unit = {},
    viewModel: TranslateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    var micPermissionDenied by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            viewModel.startListening()
        } else {
            micPermissionDenied = true
        }
    }

    TranslateScreenContent(
        uiState = uiState,
        micPermissionDenied = micPermissionDenied,
        onInputChanged = viewModel::onInputChanged,
        onSourceLanguageSelected = viewModel::onSourceLanguageSelected,
        onTargetLanguageSelected = viewModel::onTargetLanguageSelected,
        onSwapLanguagesClick = viewModel::swapLanguages,
        onTranslateClick = viewModel::translate,
        onDownloadClick = viewModel::downloadMissingModels,
        onWifiOnlyToggle = viewModel::onWifiOnlyToggled,
        onAutoDetectToggle = viewModel::onAutoDetectToggled,
        onHistoryClick = onHistoryClick,
        onListenClick = viewModel::speakTranslation,
        onStopListeningClick = viewModel::stopSpeaking,
        onMicClick = {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                micPermissionDenied = false
                viewModel.startListening()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TranslateScreenContent(
    uiState: TranslateUiState,
    micPermissionDenied: Boolean,
    onInputChanged: (String) -> Unit,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onSwapLanguagesClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onAutoDetectToggle: (Boolean) -> Unit,
    onHistoryClick: () -> Unit,
    onListenClick: () -> Unit,
    onStopListeningClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopActionRow(onHistoryClick = onHistoryClick)

        AutoDetectRow(
            enabled = uiState.isAutoDetectEnabled,
            onToggle = onAutoDetectToggle
        )

        LanguageSelectorRow(
            sourceLanguage = uiState.sourceLanguage,
            targetLanguage = uiState.targetLanguage,
            autoDetectEnabled = uiState.isAutoDetectEnabled,
            onSourceLanguageSelected = onSourceLanguageSelected,
            onTargetLanguageSelected = onTargetLanguageSelected,
            onSwapLanguagesClick = onSwapLanguagesClick
        )

        val modelsMissing = !uiState.sourceLanguageDownloaded || !uiState.targetLanguageDownloaded
        if (modelsMissing) {
            DownloadStatusBanner(
                isDownloading = uiState.isDownloadingModel,
                wifiOnly = uiState.downloadWifiOnly,
                errorMessage = uiState.downloadError,
                onDownloadClick = onDownloadClick,
                onWifiOnlyToggle = onWifiOnlyToggle
            )
        }

        InputFieldWithMic(
            uiState = uiState,
            micPermissionDenied = micPermissionDenied,
            onInputChanged = onInputChanged,
            onMicClick = onMicClick
        )

        Button(
            onClick = onTranslateClick,
            enabled = uiState.inputText.isNotBlank() &&
                    !uiState.isTranslating &&
                    !uiState.isDetectingLanguage,
            modifier = Modifier.fillMaxWidth()
        ) {
            when {
                uiState.isDetectingLanguage -> {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                uiState.isTranslating -> {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                }
                else -> {
                    Text(stringResource(R.string.translate_button))
                }
            }
        }

        ResultCard(
            uiState = uiState,
            onListenClick = onListenClick,
            onStopListeningClick = onStopListeningClick
        )
    }
}

@Composable
private fun InputFieldWithMic(
    uiState: TranslateUiState,
    micPermissionDenied: Boolean,
    onInputChanged: (String) -> Unit,
    onMicClick: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = uiState.inputText,
            onValueChange = onInputChanged,
            label = { Text(stringResource(R.string.translate_input_hint)) },
            trailingIcon = {
                IconButton(onClick = onMicClick, enabled = !uiState.isListening) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = stringResource(R.string.translate_mic_action),
                        tint = if (uiState.isListening) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )

        if (uiState.isListening) {
            Text(
                text = stringResource(R.string.translate_listening_indicator),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (micPermissionDenied) {
            Text(
                text = stringResource(R.string.translate_mic_permission_denied),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (uiState.listeningError != null) {
            Text(
                text = uiState.listeningError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TopActionRow(onHistoryClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onHistoryClick) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = stringResource(R.string.translate_history_action),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AutoDetectRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.translate_auto_detect_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}

@Composable
private fun LanguageSelectorRow(
    sourceLanguage: Language,
    targetLanguage: Language,
    autoDetectEnabled: Boolean,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onSwapLanguagesClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LanguageDropdown(
            label = stringResource(R.string.translate_from_label),
            selected = sourceLanguage,
            options = Language.supported,
            onSelected = onSourceLanguageSelected,
            enabled = !autoDetectEnabled,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onSwapLanguagesClick, enabled = !autoDetectEnabled) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = stringResource(R.string.translate_swap_languages),
                tint = if (autoDetectEnabled) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }

        LanguageDropdown(
            label = stringResource(R.string.translate_to_label),
            selected = targetLanguage,
            options = Language.supported,
            onSelected = onTargetLanguageSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DownloadStatusBanner(
    isDownloading: Boolean,
    wifiOnly: Boolean,
    errorMessage: String?,
    onDownloadClick: () -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.translate_download_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.translate_wifi_only_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = wifiOnly, onCheckedChange = onWifiOnlyToggle)
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            TextButton(
                onClick = onDownloadClick,
                enabled = !isDownloading,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.translate_download_button))
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    uiState: TranslateUiState,
    onListenClick: () -> Unit,
    onStopListeningClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isDetectingLanguage -> {
                            Text(
                                text = stringResource(R.string.translate_detecting),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.detectionError != null -> {
                            Text(
                                text = uiState.detectionError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.errorMessage != null -> {
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.translatedText.isNotBlank() -> {
                            Text(
                                text = uiState.translatedText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.translate_result_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (uiState.speechError != null) {
                        Text(
                            text = uiState.speechError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (uiState.translatedText.isNotBlank()) {
                    IconButton(
                        onClick = if (uiState.isSpeaking) onStopListeningClick else onListenClick
                    ) {
                        if (uiState.isSpeaking) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = stringResource(R.string.translate_stop_listening_action),
                                tint = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = stringResource(R.string.translate_listen_action),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}