package com.erman.imageprocessing.pipeline

import android.os.SystemClock

class FrameRate(private val clockMs: () -> Long = { SystemClock.elapsedRealtime() }) {

    private var windowStart = -1L
    private var frames = 0
    private var fps: Double? = null

    fun tick(): Double? {
        val now = clockMs()
        if (windowStart < 0) {
            windowStart = now
            return fps
        }
        frames++
        val elapsed = now - windowStart
        if (elapsed >= WINDOW_MS) {
            fps = frames * 1000.0 / elapsed
            frames = 0
            windowStart = now
        }
        return fps
    }

    companion object {
        private const val WINDOW_MS = 1000L
    }
}
