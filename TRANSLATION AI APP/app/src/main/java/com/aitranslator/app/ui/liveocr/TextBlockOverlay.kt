package com.aitranslator.app.ui.liveocr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextBlockOverlay(
    uiState: LiveOcrUiState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var viewWidthPx by remember { mutableStateOf(0f) }
    var viewHeightPx by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                viewWidthPx = it.width.toFloat()
                viewHeightPx = it.height.toFloat()
            }
    ) {
        if (viewWidthPx == 0f || uiState.imageWidth == 0) return@Box

        // Only render the largest detected block, instead of one bubble
        // per block: with paragraph-dense text, several nearby blocks
        // otherwise overlap into an unreadable mess. Showing the single
        // most prominent block matches the actual use case (translate
        // what you're pointing at) without needing bubble-layout logic.
        val primaryBlock = uiState.detectedBlocks.maxByOrNull { block ->
            val area = (block.boundingBox.right - block.boundingBox.left).toLong() *
                    (block.boundingBox.bottom - block.boundingBox.top).toLong()
            area
        } ?: return@Box

        val mapped = CoordinateMapper.mapRect(
            rect = primaryBlock.boundingBox,
            imageWidth = uiState.imageWidth,
            imageHeight = uiState.imageHeight,
            rotationDegrees = uiState.rotationDegrees,
            viewWidth = viewWidthPx,
            viewHeight = viewHeightPx
        )

        val leftDp: Dp
        val topDp: Dp
        val minWidthDp: Dp
        val minHeightDp: Dp
        with(density) {
            leftDp = mapped.left.toDp()
            topDp = mapped.top.toDp()
            minWidthDp = mapped.width.toDp()
            minHeightDp = mapped.height.toDp()
        }

        val translated = uiState.translations[TextNormalizer.normalize(primaryBlock.text)]

        Box(
            modifier = Modifier
                .offset(x = leftDp, y = topDp)
                .widthIn(min = minWidthDp, max = 260.dp)
                .heightIn(min = minHeightDp)
                .background(
                    Color.Black.copy(alpha = 0.75f),
                    RoundedCornerShape(4.dp)
                )
                .padding(6.dp)
        ) {
            Text(
                text = translated ?: "…",
                color = Color.White,
                fontSize = 13.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}