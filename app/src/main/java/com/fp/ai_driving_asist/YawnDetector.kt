package com.fp.ai_driving_asist

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.pow
import kotlin.math.sqrt

data class YawnResult(val isYawning: Boolean, val mar: Float)

class YawnDetector {

    private val MAR_THRESHOLD = 0.5f
    private val MOUTH =
            listOf(
                    13, // Upper lip
                    14, // Lower lip
                    61, // Left corner
                    291 // Right corner
            )

    fun process(landmarks: List<NormalizedLandmark>, width: Int, height: Int): YawnResult {
        val mar = calculateMAR(landmarks, MOUTH, width, height)
        val isYawning = mar > MAR_THRESHOLD

        return YawnResult(isYawning = isYawning, mar = mar)
    }

    private fun calculateMAR(
            landmarks: List<NormalizedLandmark>,
            indices: List<Int>,
            width: Int,
            height: Int
    ): Float {
        val p1 = landmarks[indices[0]] // Upper lip
        val p2 = landmarks[indices[1]] // Lower lip
        val p3 = landmarks[indices[2]] // Left corner
        val p4 = landmarks[indices[3]] // Right corner

        // Vertical distance (Upper lip to Lower lip)
        val verticalDist = distance(p1, p2, width, height)

        // Horizontal distance (Left corner to Right corner)
        val horizontalDist = distance(p3, p4, width, height)

        if (horizontalDist == 0f) return 0f

        return verticalDist / horizontalDist
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
}
