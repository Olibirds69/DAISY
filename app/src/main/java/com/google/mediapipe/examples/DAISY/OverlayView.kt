package com.google.mediapipe.examples.DAISY

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.examples.gesturerecognizer.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: List<GestureRecognizerResult>? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        linePaint.reset()
        pointPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.forEach { gestureRecognizerResult ->
            gestureRecognizerResult.landmarks().forEach { landmark ->
                landmark.forEach { normalizedLandmark ->
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                }
                HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                    val startLandmark = landmark[connection.start()]
                    val endLandmark = landmark[connection.end()]
                    canvas.drawLine(
                        startLandmark.x() * imageWidth * scaleFactor,
                        startLandmark.y() * imageHeight * scaleFactor,
                        endLandmark.x() * imageWidth * scaleFactor,
                        endLandmark.y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
        }
    }


    fun setResults(
        gestureRecognizerResults: GestureRecognizerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = listOf(gestureRecognizerResults)

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth
        scaleFactor = when (runningMode) {
            /*Para sa LIVE_STREAM gawin 1f yung Width at Height Para makita yung markers
            * Gawing 0f yung Width at Height sa Final Product ng project*/
            RunningMode.IMAGE, RunningMode.VIDEO -> min(width * 0f / imageWidth, height * 0f / imageHeight)
            RunningMode.LIVE_STREAM -> max(width * 0f / imageWidth, height * 0f / imageHeight)
        }
        invalidate()
    }


    companion object {
        /*Para lang to sa landmark ng kamay*/
        private const val LANDMARK_STROKE_WIDTH = 0F
    }
}
