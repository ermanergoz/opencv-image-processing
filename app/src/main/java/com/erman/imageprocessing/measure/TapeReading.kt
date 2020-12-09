package com.erman.imageprocessing.measure

data class TapeReading(
        val number: Int,
        val strip: TapeStrip,
        val lengthCm: Double?,
        val widthCm: Double?,
        val distanceCm: Double?
) {
    companion object {
        fun of(measurement: Measurement, pxPerCm: Double?): List<TapeReading> =
                measurement.tapes.mapIndexed { i, strip ->
                    TapeReading(
                            number = i + 1,
                            strip = strip,
                            lengthCm = pxPerCm?.let { strip.lengthPx / it },
                            widthCm = pxPerCm?.let { strip.widthPx / it },
                            distanceCm = distanceCm(measurement.reference, strip, pxPerCm)
                    )
                }

        private fun distanceCm(reference: Reference?, strip: TapeStrip, pxPerCm: Double?): Double? {
            if (reference == null || pxPerCm == null) return null
            val dx = strip.box.center.x - reference.center.x
            val dy = strip.box.center.y - reference.center.y
            return Math.hypot(dx, dy) / pxPerCm
        }
    }
}
