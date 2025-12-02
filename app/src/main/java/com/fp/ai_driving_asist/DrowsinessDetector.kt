package com.fp.ai_driving_asist
class DrowsinessDetector(
    private val drowsyThresholdMs: Long = 2000L // 2 detik
) {
    private var eyeClosedStartTimestamp: Long? = null

    fun update(eyesClosed: Boolean, frameTimestampMs: Long): Boolean {
        if (eyesClosed) {
            if (eyeClosedStartTimestamp == null) {
                eyeClosedStartTimestamp = frameTimestampMs
            }
            val duration = frameTimestampMs - (eyeClosedStartTimestamp ?: frameTimestampMs)
            return duration >= drowsyThresholdMs
        } else {
            eyeClosedStartTimestamp = null
            return false
        }
    }

    fun reset() {
        eyeClosedStartTimestamp = null
    }
}
