package com.fp.ai_driving_asist
class DrowsinessDetector(
        private val drowsyThresholdMs: Long = 2000L, // 2 detik
        private val sleepThresholdMs: Long = 5000L, // 5 detik untuk tidur
        private val yawnThresholdMs: Long = 2000L // 2 detik untuk menguap
) {
    private var eyeClosedStartTimestamp: Long? = null
    private var yawnStartTimestamp: Long? = null

    enum class DrowsinessState {
        SAFE,
        DROWSY,
        SLEEPING
    }

    fun update(eyesClosed: Boolean, isYawning: Boolean, frameTimestampMs: Long): DrowsinessState {
        var state = DrowsinessState.SAFE

        // Logika Mata Tertutup & Tidur
        if (eyesClosed) {
            if (eyeClosedStartTimestamp == null) {
                eyeClosedStartTimestamp = frameTimestampMs
            }
            val duration = frameTimestampMs - (eyeClosedStartTimestamp ?: frameTimestampMs)

            if (duration >= sleepThresholdMs) {
                state = DrowsinessState.SLEEPING
            } else if (duration >= drowsyThresholdMs) {
                state = DrowsinessState.DROWSY
            }
        } else {
            eyeClosedStartTimestamp = null
        }

        // Jika sudah SLEEPING, prioritas tertinggi, return langsung (atau cek yawn juga?)
        // Kita asumsikan SLEEPING > DROWSY.
        if (state == DrowsinessState.SLEEPING) return state

        // Logika Menguap
        if (isYawning) {
            if (yawnStartTimestamp == null) {
                yawnStartTimestamp = frameTimestampMs
            }
            val duration = frameTimestampMs - (yawnStartTimestamp ?: frameTimestampMs)
            if (duration >= yawnThresholdMs) {
                // Jika sedang DROWSY karena mata, tetap DROWSY.
                // Jika SAFE, jadi DROWSY.
                state = DrowsinessState.DROWSY
            }
        } else {
            yawnStartTimestamp = null
        }

        return state
    }

    fun reset() {
        eyeClosedStartTimestamp = null
        yawnStartTimestamp = null
    }
}
