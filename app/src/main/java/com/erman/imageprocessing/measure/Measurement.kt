package com.erman.imageprocessing.measure

import org.opencv.core.Point
import org.opencv.core.RotatedRect

const val REFERENCE_DIAMETER_CM = 5.0

data class Reference(val center: Point, val radiusPx: Double) {
    val pxPerCm: Double get() = 2 * radiusPx / REFERENCE_DIAMETER_CM
}

data class TapeStrip(val box: RotatedRect, val cutOff: Boolean = false) {
    val lengthPx: Double get() = Math.max(box.size.width, box.size.height)
    val widthPx: Double get() = Math.min(box.size.width, box.size.height)
}

data class Measurement(val reference: Reference?, val tapes: List<TapeStrip>)
