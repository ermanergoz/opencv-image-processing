package com.erman.imageprocessing.measure

import android.support.test.runner.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

@RunWith(AndroidJUnit4::class)
class SyntheticMeasurementTest {

    companion object {
        private const val PX_PER_CM = 40.0

        /**
         * One pixel of the 640 px working image, in cm. A drawn polygon includes its edge
         * pixels and a rotated edge is a staircase, so a narrow side can be off by about that
         */
        private const val WORKING_PIXEL_CM = (1280.0 / 640.0) / PX_PER_CM

        @BeforeClass
        @JvmStatic
        fun loadOpenCv() {
            assertTrue(OpenCVLoader.initDebug())
        }
    }

    private val frame = Mat(720, 1280, CvType.CV_8UC4, Scalar(128.0, 128.0, 128.0, 255.0))
    private val measurer = TapeMeasurer()

    @After
    fun release() {
        measurer.release()
        frame.release()
    }

    private fun drawCap(center: Point) =
            Imgproc.circle(frame, center, 100, Scalar(220.0, 20.0, 20.0, 255.0), -1)

    private fun drawStrip(box: RotatedRect) {
        val corners = arrayOfNulls<Point>(4)
        box.points(corners)
        Imgproc.fillConvexPoly(frame, MatOfPoint(*corners.requireNoNulls()), Scalar(20.0, 40.0, 200.0, 255.0))
    }

    @Test
    fun measuresKnownSizes() {
        drawCap(Point(250.0, 250.0))
        drawStrip(RotatedRect(Point(800.0, 450.0), Size(12 * PX_PER_CM, 2 * PX_PER_CM), 20.0))

        val measurement = measurer.measure(frame)
        val reference = measurement.reference
        assertNotNull(reference)
        assertEquals(PX_PER_CM, reference!!.pxPerCm, PX_PER_CM * 0.02)

        val reading = TapeReading.of(measurement, reference.pxPerCm).single()
        assertEquals(12.0, reading.lengthCm!!, 12.0 * 0.02)
        assertEquals(2.0, reading.widthCm!!, 2.0 * 0.02 + WORKING_PIXEL_CM)
        assertEquals(Math.hypot(550.0, 200.0) / PX_PER_CM, reading.distanceCm!!, 0.2)
        assertFalse(reading.strip.cutOff)
    }

    @Test
    fun stripsAreNumberedLeftToRight() {
        drawCap(Point(640.0, 150.0))
        drawStrip(RotatedRect(Point(1000.0, 500.0), Size(400.0, 60.0), 0.0))
        drawStrip(RotatedRect(Point(300.0, 500.0), Size(300.0, 60.0), 0.0))

        val tapes = measurer.measure(frame).tapes
        assertEquals(2, tapes.size)
        assertTrue(tapes[0].box.center.x < tapes[1].box.center.x)
    }

    @Test
    fun aStripRunningOffTheFrameIsMarkedCutOff() {
        drawCap(Point(640.0, 150.0))
        drawStrip(RotatedRect(Point(1200.0, 500.0), Size(400.0, 60.0), 0.0))

        val strip = measurer.measure(frame).tapes.single()
        assertTrue(strip.cutOff)
    }

    @Test
    fun noRedCapMeansNoScale() {
        drawStrip(RotatedRect(Point(640.0, 360.0), Size(400.0, 60.0), 0.0))

        val measurement = measurer.measure(frame)
        assertEquals(null, measurement.reference)
        assertEquals(1, measurement.tapes.size)
    }
}
