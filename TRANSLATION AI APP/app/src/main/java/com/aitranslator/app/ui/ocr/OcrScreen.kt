package com.aitranslator.app.ui.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.aitranslator.app.R
import com.aitranslator.app.domain.translation.Language
import com.aitranslator.app.ui.translate.LanguageDropdown

@Composable
fun OcrScreen(
    modifier: Modifier = Modifier,
    onLiveTranslateClick: () -> Unit = {},
    viewModel: OcrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            pendingCameraUri?.let { viewModel.onImageSelected(it) }
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            viewModel.onCameraPermissionGranted()
            val uri = viewModel.prepareCameraCaptureUri()
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            viewModel.onCameraPermissionDenied()
        }
    }

    OcrScreenContent(
        uiState = uiState,
        onPickImageClick = {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onTakePhotoClick = {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                viewModel.onCameraPermissionGranted()
                val uri = viewModel.prepareCameraCaptureUri()
                pendingCameraUri = uri
                takePictureLauncher.launch(uri)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        },
        onLiveTranslateClick = onLiveTranslateClick,
        onSourceLanguageSelected = viewModel::onSourceLanguageSelected,
        onTargetLanguageSelected = viewModel::onTargetLanguageSelected,
        onTranslateClick = viewModel::translateExtractedText,
        modifier = modifier
    )
}

@Composable
private fun OcrScreenContent(
    uiState: OcrUiState,
    onPickImageClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onLiveTranslateClick: () -> Unit,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onTranslateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onTakePhotoClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = null)
                Text(
                    text = stringResource(R.string.ocr_take_photo),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedButton(
                onClick = onPickImageClick,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Filled.Image, contentDescription = null)
                Text(
                    text = stringResource(R.string.ocr_choose_image),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        OutlinedButton(
            onClick = onLiveTranslateClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.live_ocr_open_button))
        }

        if (uiState.cameraPermissionDenied) {
            Text(
                text = stringResource(R.string.ocr_camera_permission_denied),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        uiState.imageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = stringResource(R.string.ocr_selected_image_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
        }

        when {
            uiState.isExtracting -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 12.dp))
                    Text(stringResource(R.string.ocr_extracting))
                }
            }
            uiState.extractionError != null -> {
                Text(
                    text = uiState.extractionError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            uiState.extractedText.isNotBlank() -> {
                ExtractedTextSection(
                    uiState = uiState,
                    onSourceLanguageSelected = onSourceLanguageSelected,
                    onTargetLanguageSelected = onTargetLanguageSelected,
                    onTranslateClick = onTranslateClick
                )
            }
            uiState.imageUri == null -> {
                Text(
                    text = stringResource(R.string.ocr_no_text_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExtractedTextSection(
    uiState: OcrUiState,
    onSourceLanguageSelected: (Language) -> Unit,
    onTargetLanguageSelected: (Language) -> Unit,
    onTranslateClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.ocr_extracted_text_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = uiState.extractedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageDropdown(
                label = stringResource(R.string.translate_from_label),
                selected = uiState.sourceLanguage,
                options = Language.supported,
                onSelected = onSourceLanguageSelected,
                modifier = Modifier.weight(1f)
            )
            LanguageDropdown(
                label = stringResource(R.string.translate_to_label),
                selected = uiState.targetLanguage,
                options = Language.supported,
                onSelected = onTargetLanguageSelected,
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onTranslateClick,
            enabled = !uiState.isTranslating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isTranslating) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(stringResource(R.string.ocr_translate_button))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when {
                    uiState.translationError != null -> {
                        Text(
                            text = uiState.translationError,
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
                            text = stringResource(R.string.ocr_translated_result_placeholder),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}