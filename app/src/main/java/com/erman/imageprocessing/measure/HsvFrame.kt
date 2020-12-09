package com.erman.imageprocessing.measure

import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class HsvFrame(private val processWidth: Int = 640) {

    private val small = Mat()
    private val rgb = Mat()

    val hsv = Mat()

    var scale = 1.0
        private set

    val area: Double get() = hsv.rows().toDouble() * hsv.cols()

    fun load(rgba: Mat) {
        scale = if (rgba.cols() > processWidth) processWidth.toDouble() / rgba.cols() else 1.0
        if (scale < 1.0) {
            Imgproc.resize(rgba, small, Size(rgba.cols() * scale, rgba.rows() * scale), 0.0, 0.0, Imgproc.INTER_LINEAR)
        } else {
            rgba.copyTo(small)
        }
        Imgproc.cvtColor(small, rgb, Imgproc.COLOR_RGBA2RGB)
        Imgproc.cvtColor(rgb, hsv, Imgproc.COLOR_RGB2HSV)
    }

    fun release() {
        small.release()
        rgb.release()
        hsv.release()
    }
}
