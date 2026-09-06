package com.aitranslator.app.ui.liveocr

data class DetectedTextBlock(
    val text: String,
    val boundingBox: BlockBounds
)