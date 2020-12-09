package com.erman.imageprocessing.overlay

import com.erman.imageprocessing.measure.Measurement
import com.erman.imageprocessing.measure.REFERENCE_DIAMETER_CM
import com.erman.imageprocessing.measure.Reference
import com.erman.imageprocessing.measure.TapeReading
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import java.util.Locale

class MeasurementRenderer {

    private val shade = Mat()

    fun draw(rgba: Mat, measurement: Measurement, readings: List<TapeReading>, pxPerCm: Double?, fps: Double?) {
        val pen = Pen(rgba)
        val reference = measurement.reference
        readings.forEach { drawTape(pen, it, reference) }
        if (reference != null) drawReference(pen, reference)
        drawPanel(pen, summary(readings, pxPerCm, fps))
    }

    private fun drawReference(pen: Pen, reference: Reference) {
        pen.circle(reference.center, reference.radiusPx, Palette.REFERENCE)
        pen.cross(reference.center, reference.radiusPx * 0.15, Palette.REFERENCE)
        val labelY = reference.center.y - reference.radiusPx - pen.lineHeight * 0.6
        pen.textCentered("REF ${cm(REFERENCE_DIAMETER_CM)}", Point(reference.center.x, labelY), Palette.REFERENCE)
    }

    private fun drawTape(pen: Pen, reading: TapeReading, reference: Reference?) {
        val corners = arrayOfNulls<Point>(4)
        reading.strip.box.points(corners)
        val p = corners.requireNoNulls()
        for (i in 0..3) pen.line(p[i], p[(i + 1) % 4], Palette.TAPE)

        val center = reading.strip.box.center
        val sides = (0..3).map { midpoint(p[it], p[(it + 1) % 4]) }
        val firstIsLong = distance(p[0], p[1]) >= distance(p[1], p[2])
        val longSides = if (firstIsLong) listOf(sides[0], sides[2]) else listOf(sides[1], sides[3])
        val shortSide = if (firstIsLong) sides[1] else sides[0]
        val away = reference?.center ?: center
        val lengthSide = longSides.maxBy { distance(it, away) } ?: longSides[0]
        reading.lengthCm?.let { pen.textCentered(length(reading, it), lengthSide, Palette.TAPE) }
        reading.widthCm?.let { pen.textCentered(cm(it), shortSide, Palette.TAPE) }

        if (reference != null && reading.distanceCm != null) {
            pen.line(reference.center, center, Palette.DISTANCE)
            pen.textCentered(distance(reading, reading.distanceCm), midpoint(reference.center, center), Palette.DISTANCE)
        }
        pen.textCentered("#${reading.number}", center, Palette.TEXT)
    }

    private fun summary(readings: List<TapeReading>, pxPerCm: Double?, fps: Double?): List<Pair<String, Scalar>> {
        val lines = ArrayList<Pair<String, Scalar>>()
        lines.add(Pair("WELD POINT TAPE MEASURE" + (fps?.let { "   ${Math.round(it)} FPS" } ?: ""), Palette.TEXT))
        lines.add(if (pxPerCm == null) {
            Pair("Show the red reference cap (${cm(REFERENCE_DIAMETER_CM)} across)", Palette.WARNING)
        } else {
            Pair("Scale: ${format(pxPerCm)} px/cm from the red cap", Palette.REFERENCE)
        })
        if (readings.isEmpty()) lines.add(Pair("No tape in view", Palette.TEXT))
        readings.forEach { lines.add(Pair(describe(it), Palette.TAPE)) }
        return lines
    }

    private fun describe(reading: TapeReading): String {
        val size = if (reading.lengthCm != null && reading.widthCm != null) {
            val cutOff = if (reading.strip.cutOff) " (cut off at the edge)" else ""
            "${length(reading, reading.lengthCm).removeSuffix(" cm")} x ${cm(reading.widthCm)}$cutOff"
        } else {
            "size needs the red cap"
        }
        val distance = reading.distanceCm?.let { ",  ${distance(reading, it)} from ref" } ?: ""
        return "#${reading.number}  $size$distance"
    }

    private fun drawPanel(pen: Pen, lines: List<Pair<String, Scalar>>) {
        val canvas = pen.canvas
        val pad = pen.lineHeight / 2
        val width = Math.min(canvas.cols(), (lines.map { pen.textSize(it.first).width }.max() ?: 0.0).toInt() + 2 * pad)
        val height = Math.min(canvas.rows(), lines.size * pen.lineHeight + pad)
        val box = Rect(0, canvas.rows() - height, width, height)

        val area = canvas.submat(box)
        if (shade.rows() != height || shade.cols() != width || shade.type() != canvas.type()) {
            shade.create(height, width, canvas.type())
            shade.setTo(Palette.SHADE)
        }
        Core.addWeighted(area, 0.35, shade, 0.65, 0.0, area)
        area.release()

        lines.forEachIndexed { i, line ->
            pen.text(line.first, Point(pad.toDouble(), (box.y + (i + 1) * pen.lineHeight).toDouble()), line.second)
        }
    }

    fun release() = shade.release()

    private fun length(reading: TapeReading, lengthCm: Double) = (if (reading.strip.cutOff) ">" else "") + cm(lengthCm)

    private fun distance(reading: TapeReading, distanceCm: Double) = (if (reading.strip.cutOff) "~" else "") + cm(distanceCm)

    private fun cm(value: Double) = "${format(value)} cm"

    private fun format(value: Double) = String.format(Locale.US, "%.1f", value)

    private fun distance(a: Point, b: Point) = Math.hypot(a.x - b.x, a.y - b.y)

    private fun midpoint(a: Point, b: Point) = Point((a.x + b.x) / 2, (a.y + b.y) / 2)
}
