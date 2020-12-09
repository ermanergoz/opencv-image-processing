package com.erman.imageprocessing.measure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Size

class TapeReadingTest {

    private val reference = Reference(Point(100.0, 100.0), radiusPx = 50.0)
    private val strip = TapeStrip(RotatedRect(Point(400.0, 500.0), Size(30.0, 200.0), 90.0))

    @Test
    fun scaleComesFromTheCapDiameter() {
        assertEquals(20.0, reference.pxPerCm, 1e-9)
    }

    @Test
    fun convertsPixelsToCentimetres() {
        val reading = TapeReading.of(Measurement(reference, listOf(strip)), reference.pxPerCm).single()
        assertEquals(1, reading.number)
        assertEquals(10.0, reading.lengthCm!!, 1e-9)
        assertEquals(1.5, reading.widthCm!!, 1e-9)
        assertEquals(25.0, reading.distanceCm!!, 1e-9)
    }

    @Test
    fun noScaleMeansNoCentimetres() {
        val reading = TapeReading.of(Measurement(null, listOf(strip)), null).single()
        assertNull(reading.lengthCm)
        assertNull(reading.widthCm)
        assertNull(reading.distanceCm)
    }

    @Test
    fun heldScaleWithoutReferenceGivesSizeButNoDistance() {
        val reading = TapeReading.of(Measurement(null, listOf(strip)), 20.0).single()
        assertEquals(10.0, reading.lengthCm!!, 1e-9)
        assertNull(reading.distanceCm)
    }
}
