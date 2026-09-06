package com.aitranslator.app.ui.liveocr

import org.junit.Assert.assertEquals
import org.junit.Test

class CoordinateMapperTest {

    @Test
    fun `no rotation maps coordinates directly when view matches image aspect ratio`() {
        val rect = BlockBounds(100, 100, 300, 200)

        val result = CoordinateMapper.mapRect(
            rect = rect,
            imageWidth = 1000,
            imageHeight = 1000,
            rotationDegrees = 0,
            viewWidth = 1000f,
            viewHeight = 1000f
        )

        assertEquals(100f, result.left, 0.01f)
        assertEquals(100f, result.top, 0.01f)
        assertEquals(200f, result.width, 0.01f)
        assertEquals(100f, result.height, 0.01f)
    }

    @Test
    fun `90 degree rotation swaps the effective image dimensions`() {
        val rect = BlockBounds(0, 0, 1000, 100)

        val result = CoordinateMapper.mapRect(
            rect = rect,
            imageWidth = 1000,
            imageHeight = 500,
            rotationDegrees = 90,
            viewWidth = 500f,
            viewHeight = 1000f
        )

        assertEquals(400f, result.left, 0.01f)
        assertEquals(0f, result.top, 0.01f)
        assertEquals(100f, result.width, 0.01f)
        assertEquals(1000f, result.height, 0.01f)
    }

    @Test
    fun `scaling centers content when view is wider than the rotated image`() {
        val rect = BlockBounds(0, 0, 100, 100)

        val result = CoordinateMapper.mapRect(
            rect = rect,
            imageWidth = 100,
            imageHeight = 100,
            rotationDegrees = 0,
            viewWidth = 200f,
            viewHeight = 100f
        )

        assertEquals(0f, result.left, 0.01f)
        assertEquals(200f, result.width, 0.01f)
    }

    @Test
    fun `180 degree rotation flips the rect to the opposite corner`() {
        val rect = BlockBounds(0, 0, 100, 50)

        val result = CoordinateMapper.mapRect(
            rect = rect,
            imageWidth = 1000,
            imageHeight = 500,
            rotationDegrees = 180,
            viewWidth = 1000f,
            viewHeight = 500f
        )

        assertEquals(900f, result.left, 0.01f)
        assertEquals(450f, result.top, 0.01f)
        assertEquals(100f, result.width, 0.01f)
        assertEquals(50f, result.height, 0.01f)
    }
}