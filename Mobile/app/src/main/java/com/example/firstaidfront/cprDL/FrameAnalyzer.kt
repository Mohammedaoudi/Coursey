package com.example.firstaidfront.cprDL



import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import kotlin.math.abs

class FrameAnalyzer {
    private val frameBuffer = mutableListOf<Bitmap>()
    private val signHistory = mutableListOf<Int>()
    private val savedImages = mutableListOf<Bitmap>()
    private var count = 0.0

    fun addFrame(frame: Bitmap) {
        val resizedFrame = Bitmap.createScaledBitmap(frame, 224, 224, true)

        if (frameBuffer.isNotEmpty()) {
            val prevFrame = frameBuffer.last()
            val verticalMean = calculateVerticalMovement(prevFrame, resizedFrame)

            val currentSign = when {
                verticalMean > 0 -> 1
                verticalMean < 0 -> -1
                else -> 0
            }

            if (signHistory.isNotEmpty() && currentSign != signHistory.last()) {
                if (signHistory.size >= 3) {
                    savedImages.add(Bitmap.createBitmap(resizedFrame))
                    count += 0.5
                }
                signHistory.clear()
                signHistory.add(currentSign)
            } else {
                signHistory.add(currentSign)
            }
        }

        frameBuffer.add(resizedFrame)
    }

    // No need for hasEnoughFrames() check anymore since we're processing by segments
// Add this method to FrameAnalyzer
    fun getBufferSize(): Int = frameBuffer.size
    fun getCriticalFrame(): Bitmap? {
        if (savedImages.isEmpty()) return null

        // Average the saved images
        val result = Bitmap.createBitmap(224, 224, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            alpha = (255f / savedImages.size).toInt()
        }

        savedImages.forEach { image ->
            canvas.drawBitmap(image, 0f, 0f, paint)
        }

        return result
    }

    fun getCompressionCount(): Int = (count * 12).toInt() // Convert to BPM as in original

    fun reset() {
        frameBuffer.forEach { it.recycle() }
        frameBuffer.clear()
        savedImages.forEach { it.recycle() }
        savedImages.clear()
        signHistory.clear()
        count = 0.0
    }

    private fun calculateVerticalMovement(prev: Bitmap, curr: Bitmap): Float {
        var totalDiff = 0f
        val height = prev.height
        val width = prev.width

        // Focus on central region
        val centerStartX = width * 3 / 8
        val centerEndX = width * 5 / 8

        for (x in centerStartX until centerEndX) {
            for (y in 0 until height) {
                val prevPixel = prev.getPixel(x, y)
                val currPixel = curr.getPixel(x, y)

                val prevLum = (Color.red(prevPixel) * 0.299f +
                        Color.green(prevPixel) * 0.587f +
                        Color.blue(prevPixel) * 0.114f)
                val currLum = (Color.red(currPixel) * 0.299f +
                        Color.green(currPixel) * 0.587f +
                        Color.blue(currPixel) * 0.114f)

                totalDiff += (currLum - prevLum)
            }
        }

        return totalDiff / (height * (centerEndX - centerStartX))
    }
}