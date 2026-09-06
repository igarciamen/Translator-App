package com.aitranslator.app.ui.liveocr

/**
 * Plain Kotlin equivalent of android.graphics.Rect, used anywhere this
 * data needs to be exercised by plain JVM unit tests. android.graphics.Rect
 * is not reliable in non-instrumented tests: its stub constructor silently
 * leaves all fields at 0 instead of throwing, unlike most other Android
 * stub classes, which makes bugs here very hard to notice.
 */
data class BlockBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)