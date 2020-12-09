package com.erman.imageprocessing.measure

import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.RotatedRect
import org.opencv.core.Size

class TapeMeasurer(
        private val frame: HsvFrame = HsvFrame(),
        private val referenceDetector: ReferenceDetector = ReferenceDetector(),
        private val tapeDetector: TapeDetector = TapeDetector()
) {

    fun measure(rgba: Mat): Measurement {
        frame.load(rgba)
        val s = frame.scale
        return Measurement(
                referenceDetector.detect(frame)?.let { Reference(Point(it.center.x / s, it.center.y / s), it.radiusPx / s) },
                tapeDetector.detect(frame).map { TapeStrip(unscale(it.box, s), it.cutOff) }
        )
    }

    private fun unscale(box: RotatedRect, s: Double) =
            RotatedRect(Point(box.center.x / s, box.center.y / s), Size(box.size.width / s, box.size.height / s), box.angle)

    fun release() {
        frame.release()
        referenceDetector.release()
        tapeDetector.release()
    }
}
