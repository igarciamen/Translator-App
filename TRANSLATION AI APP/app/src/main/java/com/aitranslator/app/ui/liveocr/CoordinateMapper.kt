package com.aitranslator.app.ui.liveocr

import kotlin.math.max

object CoordinateMapper {

    fun mapRect(
        rect: BlockBounds,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int,
        viewWidth: Float,
        viewHeight: Float
    ): MappedRect {
        val rotated = rotateRect(rect, imageWidth, imageHeight, rotationDegrees)

        val scale = max(
            viewWidth / rotated.rotatedImageWidth,
            viewHeight / rotated.rotatedImageHeight
        )
        val offsetX = (viewWidth - rotated.rotatedImageWidth * scale) / 2f
        val offsetY = (viewHeight - rotated.rotatedImageHeight * scale) / 2f

        return MappedRect(
            left = rotated.left * scale + offsetX,
            top = rotated.top * scale + offsetY,
            width = (rotated.right - rotated.left) * scale,
            height = (rotated.bottom - rotated.top) * scale
        )
    }

    private data class RotatedRect(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float,
        val rotatedImageWidth: Float,
        val rotatedImageHeight: Float
    )

    private fun rotateRect(
        rect: BlockBounds,
        imageWidth: Int,
        imageHeight: Int,
        rotationDegrees: Int
    ): RotatedRect {
        return when (rotationDegrees) {
            90 -> RotatedRect(
                left = (imageHeight - rect.bottom).toFloat(),
                top = rect.left.toFloat(),
                right = (imageHeight - rect.top).toFloat(),
                bottom = rect.right.toFloat(),
                rotatedImageWidth = imageHeight.toFloat(),
                rotatedImageHeight = imageWidth.toFloat()
            )
            180 -> RotatedRect(
                left = (imageWidth - rect.right).toFloat(),
                top = (imageHeight - rect.bottom).toFloat(),
                right = (imageWidth - rect.left).toFloat(),
                bottom = (imageHeight - rect.top).toFloat(),
                rotatedImageWidth = imageWidth.toFloat(),
                rotatedImageHeight = imageHeight.toFloat()
            )
            270 -> RotatedRect(
                left = rect.top.toFloat(),
                top = (imageWidth - rect.right).toFloat(),
                right = rect.bottom.toFloat(),
                bottom = (imageWidth - rect.left).toFloat(),
                rotatedImageWidth = imageHeight.toFloat(),
                rotatedImageHeight = imageWidth.toFloat()
            )
            else -> RotatedRect(
                left = rect.left.toFloat(),
                top = rect.top.toFloat(),
                right = rect.right.toFloat(),
                bottom = rect.bottom.toFloat(),
                rotatedImageWidth = imageWidth.toFloat(),
                rotatedImageHeight = imageHeight.toFloat()
            )
        }
    }
}