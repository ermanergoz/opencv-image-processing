package com.erman.imageprocessing.measure

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.erman.imageprocessing.SamplePhoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat

@RunWith(AndroidJUnit4::class)
class SamplePhotoMeasurementTest {

    companion object {
        @BeforeClass
        @JvmStatic
        fun loadOpenCv() {
            assertTrue(OpenCVLoader.initDebug())
        }
    }

    private fun measureSamplePhoto(): Measurement {
        val photo = SamplePhoto.load(InstrumentationRegistry.getTargetContext().resources)
        val rgba = Mat()
        Utils.bitmapToMat(photo, rgba)
        photo.recycle()
        val measurer = TapeMeasurer()
        try {
            return measurer.measure(rgba)
        } finally {
            measurer.release()
            rgba.release()
        }
    }

    @Test
    fun findsTheRedReferenceCap() {
        val reference = measureSamplePhoto().reference
        assertNotNull(reference)
        assertEquals(230.0, reference!!.radiusPx, 25.0)
        assertEquals(965.0, reference.center.x, 40.0)
        assertEquals(600.0, reference.center.y, 40.0)
    }

    @Test
    fun measuresTheBlueTapeInCentimetres() {
        val measurement = measureSamplePhoto()
        val tape = TapeReading.of(measurement, measurement.reference?.pxPerCm).single()
        assertEquals(19.2, tape.lengthCm!!, 0.6)
        assertEquals(1.8, tape.widthCm!!, 0.15)
    }
}
