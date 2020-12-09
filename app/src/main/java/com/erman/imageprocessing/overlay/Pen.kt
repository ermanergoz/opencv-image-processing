package com.erman.imageprocessing.overlay

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class Pen(val canvas: Mat) {

    private val fontScale = Math.max(0.5, canvas.cols() / 1280.0 * 0.8)
    val stroke = Math.max(2, Math.round(canvas.cols() / 640.0).toInt())
    private val textStroke = Math.max(1, Math.round(fontScale * 2).toInt())

    val lineHeight: Int = (textSize("Hg").height * 1.8).toInt()

    fun line(from: Point, to: Point, color: Scalar) {
        Imgproc.line(canvas, from, to, Palette.OUTLINE, stroke + 2, Imgproc.LINE_AA, 0)
        Imgproc.line(canvas, from, to, color, stroke, Imgproc.LINE_AA, 0)
    }

    fun circle(center: Point, radius: Double, color: Scalar) {
        val r = Math.round(radius).toInt()
        Imgproc.circle(canvas, center, r, Palette.OUTLINE, stroke + 2, Imgproc.LINE_AA, 0)
        Imgproc.circle(canvas, center, r, color, stroke, Imgproc.LINE_AA, 0)
    }

    fun cross(center: Point, size: Double, color: Scalar) {
        line(Point(center.x - size, center.y), Point(center.x + size, center.y), color)
        line(Point(center.x, center.y - size), Point(center.x, center.y + size), color)
    }

    fun text(text: String, origin: Point, color: Scalar) {
        Imgproc.putText(canvas, text, origin, FONT, fontScale, Palette.OUTLINE, textStroke + 3, Imgproc.LINE_AA, false)
        Imgproc.putText(canvas, text, origin, FONT, fontScale, color, textStroke, Imgproc.LINE_AA, false)
    }

    fun textCentered(text: String, center: Point, color: Scalar) {
        val size = textSize(text)
        text(text, Point(center.x - size.width / 2, center.y + size.height / 2), color)
    }

    fun textSize(text: String): Size = Imgproc.getTextSize(text, FONT, fontScale, textStroke, null)

    companion object {
        private const val FONT = Core.FONT_HERSHEY_SIMPLEX
    }
}
