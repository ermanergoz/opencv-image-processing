package com.erman.imageprocessing.pipeline

import org.opencv.core.Mat

interface FrameProcessor {
    fun process(rgba: Mat)
    fun release()
}
