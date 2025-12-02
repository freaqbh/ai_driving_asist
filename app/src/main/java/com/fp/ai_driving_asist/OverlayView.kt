package com.fp.ai_driving_asist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private val LANDMARK_STROKE_WIDTH = 8F

    private var isDrowsy: Boolean = false

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color = Color.RED
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { faceLandmarkerResult ->

            // Calculate scaled image dimensions
            val scaledImageWidth = imageWidth * scaleFactor
            val scaledImageHeight = imageHeight * scaleFactor

            // Calculate offsets to center the image on the canvas
            val offsetX = (width - scaledImageWidth) / 2f
            val offsetY = (height - scaledImageHeight) / 2f

            // Iterate through each detected face
            faceLandmarkerResult.faceLandmarks().forEach { faceLandmarks ->
                // Draw all landmarks for the current face
                drawFaceLandmarks(canvas, faceLandmarks, offsetX, offsetY)

                // Draw all connectors for the current face
                drawFaceConnectors(canvas, faceLandmarks, offsetX, offsetY)
            }
        }
    }

    private fun drawFaceLandmarks(
            canvas: Canvas,
            faceLandmarks: List<NormalizedLandmark>,
            offsetX: Float,
            offsetY: Float
    ) {
        faceLandmarks.forEach { landmark ->
            val x = landmark.x() * imageWidth * scaleFactor + offsetX
            val y = landmark.y() * imageHeight * scaleFactor + offsetY
            canvas.drawPoint(x, y, pointPaint)
        }
    }

    private fun drawFaceConnectors(
            canvas: Canvas,
            faceLandmarks: List<NormalizedLandmark>,
            offsetX: Float,
            offsetY: Float
    ) {
        FaceLandmarker.FACE_LANDMARKS_CONNECTORS.filterNotNull().forEach { connector ->
            val startLandmark = faceLandmarks.getOrNull(connector.start())
            val endLandmark = faceLandmarks.getOrNull(connector.end())

            if (startLandmark != null && endLandmark != null) {
                val startX = startLandmark.x() * imageWidth * scaleFactor + offsetX
                val startY = startLandmark.y() * imageHeight * scaleFactor + offsetY
                val endX = endLandmark.x() * imageWidth * scaleFactor + offsetX
                val endY = endLandmark.y() * imageHeight * scaleFactor + offsetY

                canvas.drawLine(startX, startY, endX, endY, linePaint)
            }
        }
    }

    fun setDrowsyState(drowsy: Boolean) {
        isDrowsy = drowsy
        invalidate()
    }


    fun setResults(
            faceLandmarkerResults: FaceLandmarkerResult,
            imageHeight: Int,
            imageWidth: Int,
            runningMode: com.google.mediapipe.tasks.vision.core.RunningMode =
                    com.google.mediapipe.tasks.vision.core.RunningMode.LIVE_STREAM
    ) {
        results = faceLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor =
                when (runningMode) {
                    com.google.mediapipe.tasks.vision.core.RunningMode.IMAGE,
                    com.google.mediapipe.tasks.vision.core.RunningMode.VIDEO -> {
                        min(width * 1f / imageWidth, height * 1f / imageHeight)
                    }
                    com.google.mediapipe.tasks.vision.core.RunningMode.LIVE_STREAM -> {
                        // PreviewView is in FILL_CENTER mode. So we need to scale up the
                        // landmarks to match with the size that the captured images will be
                        // displayed.
                        max(width * 1f / imageWidth, height * 1f / imageHeight)
                    }
                }
        invalidate()
    }
}
