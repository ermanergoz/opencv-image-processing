package com.erman.imageprocessing.pipeline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FrameRateTest {

    @Test
    fun countsFramesPerSecond() {
        var now = 0L
        val rate = FrameRate { now }
        assertNull(rate.tick())
        for (i in 1..29) {
            now = i * 1000L / 30
            assertNull(rate.tick())
        }
        now = 1000L
        assertEquals(30.0, rate.tick()!!, 1e-9)
    }
}
