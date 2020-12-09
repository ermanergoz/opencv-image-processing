package com.erman.imageprocessing.measure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScaleFilterTest {

    @Test
    fun firstValuePassesThrough() {
        assertEquals(40.0, ScaleFilter().update(40.0)!!, 1e-9)
    }

    @Test
    fun smallChangesAreSmoothed() {
        val filter = ScaleFilter(alpha = 0.25)
        filter.update(40.0)
        assertEquals(41.0, filter.update(44.0)!!, 1e-9)
    }

    @Test
    fun oneBadFrameIsIgnored() {
        val filter = ScaleFilter(maxJump = 0.3, confirmFrames = 3)
        filter.update(40.0)
        assertEquals(40.0, filter.update(80.0)!!, 1e-9)
        assertEquals(40.0, filter.update(40.0)!!, 1e-9)
    }

    @Test
    fun aJumpThatHoldsIsAdopted() {
        val filter = ScaleFilter(maxJump = 0.3, confirmFrames = 3)
        filter.update(40.0)
        assertEquals(40.0, filter.update(80.0)!!, 1e-9)
        assertEquals(40.0, filter.update(80.0)!!, 1e-9)
        assertEquals(80.0, filter.update(80.0)!!, 1e-9)
    }

    @Test
    fun scatteredBadFramesAreNotAdopted() {
        val filter = ScaleFilter(maxJump = 0.3, confirmFrames = 3)
        filter.update(40.0)
        filter.update(80.0)
        filter.update(20.0)
        assertEquals(40.0, filter.update(80.0)!!, 1e-9)
    }

    @Test
    fun aRestartForgetsAPendingJump() {
        val filter = ScaleFilter(maxJump = 0.3, confirmFrames = 3, holdFrames = 0)
        filter.update(40.0)
        filter.update(80.0)
        filter.update(80.0)
        filter.update(null)
        filter.update(40.0)
        assertEquals(40.0, filter.update(80.0)!!, 1e-9)
    }

    @Test
    fun holdsTheScaleBrieflyWhenTheReferenceIsLost() {
        val filter = ScaleFilter(holdFrames = 2)
        filter.update(40.0)
        assertEquals(40.0, filter.update(null)!!, 1e-9)
        assertEquals(40.0, filter.update(null)!!, 1e-9)
        assertNull(filter.update(null))
    }
}
