package com.erman.imageprocessing.measure

import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.RotatedRect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class TapeDetector {

    private val blue = ColorMask(listOf(Pair(Scalar(90.0, 100.0, 40.0), Scalar(130.0, 255.0, 255.0))))

    fun detect(frame: HsvFrame): List<TapeStrip> {
        val found = ArrayList<Pair<Double, TapeStrip>>()
        for (contour in blue.contours(frame.hsv)) {
            val area = Imgproc.contourArea(contour)
            if (area >= frame.area * MIN_AREA) {
                val curve = MatOfPoint2f(*contour.toArray())
                val strip = TapeStrip(withPixelEdges(Imgproc.minAreaRect(curve)), touchesEdge(contour, frame))
                curve.release()
                if (isStrip(strip, area)) found.add(Pair(area, strip))
            }
            contour.release()
        }
        val column = frame.hsv.cols() * COLUMN_WIDTH
        return found.sortedByDescending { it.first }
                .take(MAX_STRIPS)
                .map { it.second }
                .sortedWith(compareBy({ Math.floor(it.box.center.x / column) }, { it.box.center.y }))
    }

    /** Contours run through the centres of the edge pixels: each side is one pixel short */
    private fun withPixelEdges(box: RotatedRect) =
            RotatedRect(box.center, Size(box.size.width + 1, box.size.height + 1), box.angle)

    private fun touchesEdge(contour: MatOfPoint, frame: HsvFrame): Boolean {
        val bounds = Imgproc.boundingRect(contour)
        return bounds.x <= 0 || bounds.y <= 0 ||
                bounds.x + bounds.width >= frame.hsv.cols() || bounds.y + bounds.height >= frame.hsv.rows()
    }

    private fun isStrip(strip: TapeStrip, area: Double): Boolean =
            strip.widthPx > 0 &&
                    strip.lengthPx / strip.widthPx >= MIN_ASPECT &&
                    area / (strip.lengthPx * strip.widthPx) >= MIN_FILL

    fun release() = blue.release()

    companion object {
        const val MIN_AREA = 0.002
        const val MIN_ASPECT = 2.5
        const val MIN_FILL = 0.5
        const val MAX_STRIPS = 5
        const val COLUMN_WIDTH = 0.1
    }
}
