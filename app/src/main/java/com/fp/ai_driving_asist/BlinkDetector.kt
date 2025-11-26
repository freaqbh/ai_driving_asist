package com.fp.ai_driving_asist

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.pow
import kotlin.math.sqrt

class BlinkDetector {

    private var blinkCount = 0
    private var isEyeClosed = false

    // Thresholds
    private val EAR_THRESHOLD = 0.1f // Eye Aspect Ratio threshold for closed eye

    // Indices for Left Eye
    private val LEFT_EYE = listOf(33, 160, 158, 133, 153, 144)
    // Indices for Right Eye
    private val RIGHT_EYE = listOf(362, 385, 387, 263, 373, 380)

    fun process(landmarks: List<NormalizedLandmark>, width: Int, height: Int): Int {
        val leftEAR = calculateEAR(landmarks, LEFT_EYE, width, height)
        val rightEAR = calculateEAR(landmarks, RIGHT_EYE, width, height)

        val avgEAR = (leftEAR + rightEAR) / 2.0f

        if (avgEAR < EAR_THRESHOLD) {
            if (!isEyeClosed) {
                isEyeClosed = true
            }
        } else {
            if (isEyeClosed) {
                blinkCount++
                isEyeClosed = false
            }
        }
        return blinkCount
    }

    private fun calculateEAR(
            landmarks: List<NormalizedLandmark>,
            indices: List<Int>,
            width: Int,
            height: Int
    ): Float {
        // EAR = (|p2 - p6| + |p3 - p5|) / (2 * |p1 - p4|)
        // indices: 0=p1, 1=p2, 2=p3, 3=p4, 4=p5, 5=p6

        val p1 = landmarks[indices[0]]
        val p2 = landmarks[indices[1]]
        val p3 = landmarks[indices[2]]
        val p4 = landmarks[indices[3]]
        val p5 = landmarks[indices[4]]
        val p6 = landmarks[indices[5]]

        val dist26 = distance(p2, p6, width, height)
        val dist35 = distance(p3, p5, width, height)
        val dist14 = distance(p1, p4, width, height)

        return (dist26 + dist35) / (2.0f * dist14)
    }

    private fun distance(
            p1: NormalizedLandmark,
            p2: NormalizedLandmark,
            width: Int,
            height: Int
    ): Float {
        val x1 = p1.x() * width
        val y1 = p1.y() * height
        val x2 = p2.x() * width
        val y2 = p2.y() * height
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    fun reset() {
        blinkCount = 0
        isEyeClosed = false
    }
}
