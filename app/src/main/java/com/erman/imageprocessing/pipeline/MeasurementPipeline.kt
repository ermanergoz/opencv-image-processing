package com.erman.imageprocessing.pipeline

import com.erman.imageprocessing.measure.ScaleFilter
import com.erman.imageprocessing.measure.TapeMeasurer
import com.erman.imageprocessing.measure.TapeReading
import com.erman.imageprocessing.overlay.MeasurementRenderer
import org.opencv.core.Mat

class MeasurementPipeline(
        private val measurer: TapeMeasurer,
        private val renderer: MeasurementRenderer,
        private val scaleFilter: ScaleFilter?,
        private val frameRate: FrameRate?
) : FrameProcessor {

    override fun process(rgba: Mat) {
        val measurement = measurer.measure(rgba)
        val rawScale = measurement.reference?.pxPerCm
        val pxPerCm = if (scaleFilter != null) scaleFilter.update(rawScale) else rawScale
        renderer.draw(rgba, measurement, TapeReading.of(measurement, pxPerCm), pxPerCm, frameRate?.tick())
    }

    override fun release() {
        measurer.release()
        renderer.release()
    }

    companion object {
        fun live() = MeasurementPipeline(TapeMeasurer(), MeasurementRenderer(), ScaleFilter(), FrameRate())

        fun still() = MeasurementPipeline(TapeMeasurer(), MeasurementRenderer(), null, null)
    }
}
