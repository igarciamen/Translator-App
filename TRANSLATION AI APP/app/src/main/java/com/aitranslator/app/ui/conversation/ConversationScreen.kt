package com.aitranslator.app.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.aitranslator.app.ui.translate.LanguageDropdown

@Composable
fun ConversationScreen(
    modifier: Modifier = Modifier,
    viewModel: ConversationViewModel = hiltViewModel()
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
    var pendingMicSide by remember { mutableStateOf<Boolean?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            when (pendingMicSide) {
                true -> viewModel.onMicATapped()
                false -> viewModel.onMicBTapped()
                null -> Unit
            }
        } else {
            micPermissionDenied = true
        }
        pendingMicSide = null
    }

    fun requestMic(isSideA: Boolean) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            micPermissionDenied = false
            if (isSideA) viewModel.onMicATapped() else viewModel.onMicBTapped()
        } else {
            pendingMicSide = isSideA
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    ConversationScreenContent(
        uiState = uiState,
        micPermissionDenied = micPermissionDenied,
        onLanguageASelected = viewModel::onLanguageASelected,
        onLanguageBSelected = viewModel::onLanguageBSelected,
        onSwapSpeakersClick = viewModel::onSwapSpeakers,
        onMicAClick = { requestMic(isSideA = true) },
        onMicBClick = { requestMic(isSideA = false) },
        modifier = modifier
    )
}

@Composable
private fun ConversationScreenContent(
    uiState: ConversationUiState,
    micPermissionDenied: Boolean,
    onLanguageASelected: (Language) -> Unit,
    onLanguageBSelected: (Language) -> Unit,
    onSwapSpeakersClick: () -> Unit,
    onMicAClick: () -> Unit,
    onMicBClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SwapSpeakersRow(onSwapSpeakersClick = onSwapSpeakersClick)

        Row(modifier = Modifier.fillMaxSize()) {
            SpeakerHalf(
                labelRes = R.string.conversation_language_a_label,
                selectedLanguage = uiState.languageA,
                isListening = uiState.isListeningA,
                otherSideIsListening = uiState.isListeningB,
                isSpeaking = uiState.isSpeakingA,
                error = uiState.errorA,
                micPermissionDenied = micPermissionDenied,
                displayedText = displayedTextFor(uiState.lastMessage, isSideA = true),
                onLanguageSelected = onLanguageASelected,
                onMicClick = onMicAClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            VerticalDivider()

            SpeakerHalf(
                labelRes = R.string.conversation_language_b_label,
                selectedLanguage = uiState.languageB,
                isListening = uiState.isListeningB,
                otherSideIsListening = uiState.isListeningA,
                isSpeaking = uiState.isSpeakingB,
                error = uiState.errorB,
                micPermissionDenied = micPermissionDenied,
                displayedText = displayedTextFor(uiState.lastMessage, isSideA = false),
                onLanguageSelected = onLanguageBSelected,
                onMicClick = onMicBClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

private fun displayedTextFor(message: ConversationMessage?, isSideA: Boolean): String? {
    if (message == null) return null
    return if (message.spokenBySideA == isSideA) message.originalText else message.translatedText
}

@Composable
private fun SwapSpeakersRow(onSwapSpeakersClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(onClick = onSwapSpeakersClick) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = stringResource(R.string.conversation_swap_action),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun SpeakerHalf(
    labelRes: Int,
    selectedLanguage: Language,
    isListening: Boolean,
    otherSideIsListening: Boolean,
    isSpeaking: Boolean,
    error: String?,
    micPermissionDenied: Boolean,
    displayedText: String?,
    onLanguageSelected: (Language) -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LanguageDropdown(
                label = "",
                selected = selectedLanguage,
                options = Language.supported,
                onSelected = onLanguageSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            when {
                isListening -> {
                    Text(
                        text = stringResource(R.string.conversation_listening_indicator),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                error != null -> {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                displayedText != null -> {
                    Text(
                        text = displayedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSpeaking) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.translate_listen_action),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.conversation_empty_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (micPermissionDenied) {
                Text(
                    text = stringResource(R.string.translate_mic_permission_denied),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Surface(
            onClick = onMicClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = stringResource(R.string.conversation_mic_action),
                        tint = if (otherSideIsListening) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}