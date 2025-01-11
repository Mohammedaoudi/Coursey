package com.example.firstaidfront.cprDL


import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

class CPRPredictor(private val context: Context) {
    private var module: Module? = null
    private val NORM_MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val NORM_STD = floatArrayOf(0.229f, 0.224f, 0.225f)

    init {
        try {
            val modelPath = assetFilePath("Best_QCPR_mobile.pt")
            module = Module.load(modelPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun assetFilePath(assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    fun analyze(bitmap: Bitmap, compressionRate: Int? = null): CPRResult {
        try {
            Log.d("VideoAnalysisss", "Starting analysis of frame")
            val tensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                NORM_MEAN,
                NORM_STD
            )
            Log.d("VideoAnalysisss", "Created tensor from bitmap")

            val outputs = module?.forward(IValue.from(tensor))?.toTuple()
            Log.d("VideoAnalysisss", "Got model outputs")

            val depth = outputs?.get(0)?.toTensor()?.dataAsFloatArray?.get(0) ?: 0f
            val release = outputs?.get(1)?.toTensor()?.dataAsFloatArray?.get(0) ?: 0f
            val hand = outputs?.get(2)?.toTensor()?.dataAsFloatArray?.get(0) ?: 0f

            Log.d("VideoAnalysisss", "Raw outputs - depth: $depth, release: $release, hand: $hand")

            return CPRResult(
                depth = depth * 43 + 20,
                release = if (release >= 0.5) "Incorrect" else "Correct",
                handPosition = if (hand >= 0.5) "Incorrect" else "Correct",
                compressionRate = compressionRate
            ).also {
                Log.d("VideoAnalysisss", "Final result: $it")
            }
        } catch (e: Exception) {
            Log.e("VideoAnalysisss", "Error during analysis", e)
            throw e
        }
    }}