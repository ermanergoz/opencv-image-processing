package com.erman.imageprocessing.measure

import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class ReferenceDetector {

    private val red = ColorMask(listOf(
            Pair(Scalar(0.0, 120.0, 70.0), Scalar(10.0, 255.0, 255.0)),
            Pair(Scalar(160.0, 120.0, 70.0), Scalar(179.0, 255.0, 255.0))
    ))

    fun detect(frame: HsvFrame): Reference? {
        var best: Reference? = null
        var bestArea = 0.0
        for (contour in red.contours(frame.hsv)) {
            val area = Imgproc.contourArea(contour)
            if (area >= frame.area * MIN_AREA && area > bestArea) {
                val circle = circleOf(MatOfPoint2f(*contour.toArray()), area)
                if (circle != null) {
                    best = circle
                    bestArea = area
                }
            }
            contour.release()
        }
        return best
    }

    private fun circleOf(curve: MatOfPoint2f, area: Double): Reference? {
        val perimeter = Imgproc.arcLength(curve, true)
        val center = Point()
        val radius = FloatArray(1)
        Imgproc.minEnclosingCircle(curve, center, radius)
        curve.release()

        val circularity = 4 * Math.PI * area / (perimeter * perimeter)
        val fill = area / (Math.PI * radius[0] * radius[0])
        return if (circularity >= MIN_CIRCULARITY && fill >= MIN_FILL) Reference(center, Math.sqrt(area / Math.PI) + PIXEL_EDGE) else null
    }

    fun release() = red.release()

    companion object {
        const val MIN_AREA = 0.001
        const val MIN_CIRCULARITY = 0.7
        const val MIN_FILL = 0.7

        /** Contours run through the centres of the edge pixels, half a pixel inside the blob */
        const val PIXEL_EDGE = 0.5
    }
}
