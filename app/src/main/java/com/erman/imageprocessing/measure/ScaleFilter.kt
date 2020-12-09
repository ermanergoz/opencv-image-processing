package com.erman.imageprocessing.measure

class ScaleFilter(
        private val alpha: Double = 0.25,
        private val maxJump: Double = 0.3,
        private val confirmFrames: Int = 3,
        private val holdFrames: Int = 15
) {
    private var value: Double? = null
    private var missed = 0
    private var candidate: Double? = null
    private var candidateFrames = 0

    fun update(pxPerCm: Double?): Double? {
        if (pxPerCm == null) {
            missed++
            if (missed > holdFrames) restart()
            return value
        }
        missed = 0
        val current = value
        when {
            current == null -> value = pxPerCm
            isJump(pxPerCm, current) -> confirmJump(pxPerCm)
            else -> {
                clearCandidate()
                value = current + alpha * (pxPerCm - current)
            }
        }
        return value
    }

    private fun confirmJump(pxPerCm: Double) {
        val pending = candidate
        if (pending != null && !isJump(pxPerCm, pending)) {
            candidateFrames++
        } else {
            candidate = pxPerCm
            candidateFrames = 1
        }
        if (candidateFrames >= confirmFrames) {
            value = pxPerCm
            clearCandidate()
        }
    }

    private fun isJump(pxPerCm: Double, from: Double) = Math.abs(pxPerCm - from) / from > maxJump

    private fun restart() {
        value = null
        clearCandidate()
    }

    private fun clearCandidate() {
        candidate = null
        candidateFrames = 0
    }
}
