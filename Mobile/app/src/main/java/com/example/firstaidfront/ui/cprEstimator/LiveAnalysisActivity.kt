package com.example.firstaidfront.ui.cprEstimator

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.example.firstaidfront.R
import com.example.firstaidfront.adapter.CPRResultAdapter
import com.example.firstaidfront.cprDL.CPRPredictor
import com.example.firstaidfront.cprDL.CPRResult
import com.example.firstaidfront.cprDL.FrameAnalyzer
import com.google.android.material.button.MaterialButton
import java.util.concurrent.TimeUnit



class LiveAnalysisActivity : AppCompatActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var viewFinder: PreviewView
    private lateinit var cprPredictor: CPRPredictor
    private lateinit var frameAnalyzer: FrameAnalyzer
    private lateinit var recyclerView: RecyclerView
    private lateinit var resultAdapter: CPRResultAdapter
    private lateinit var startButton: MaterialButton

    private var isAnalyzing = false
    private var analysisStartTime = 0L
    private var frameCount = 0
    private val FRAMES_PER_SEGMENT = 145 // Same as video activity

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_analysis)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Enable back button
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        initializeViews()
        setupListeners()

        cprPredictor = CPRPredictor(this)
        frameAnalyzer = FrameAnalyzer()
        cameraExecutor = Executors.newSingleThreadExecutor()

        startCamera()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initializeViews() {
        viewFinder = findViewById(R.id.viewFinder)
        recyclerView = findViewById(R.id.recyclerView)
        startButton = findViewById(R.id.startButton)

        resultAdapter = CPRResultAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@LiveAnalysisActivity)
            adapter = resultAdapter
        }
    }



    private fun setupListeners() {
        startButton.setOnClickListener {
            isAnalyzing = !isAnalyzing
            startButton.text = if (isAnalyzing) "Stop Analysis" else "Start Analysis"
            if (isAnalyzing) {
                analysisStartTime = System.currentTimeMillis()
                frameCount = 0
                segmentCounter = 0  // Reset counter when starting
                frameAnalyzer.reset()
                resultAdapter.clear()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetResolution(android.util.Size(224, 224))
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(224, 224))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (isAnalyzing) {
                            processFrame(imageProxy)
                        }
                        imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch(exc: Exception) {
                Log.e("LiveAnalysis", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private var segmentCounter = 0

    private fun processFrame(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        frameAnalyzer.addFrame(bitmap)
        frameCount++

        if (frameCount >= FRAMES_PER_SEGMENT) {
            val criticalFrame = frameAnalyzer.getCriticalFrame()
            val compressionCount = frameAnalyzer.getCompressionCount()

            criticalFrame?.let { frame ->
                val result = cprPredictor.analyze(frame, compressionCount)
                runOnUiThread {
                    resultAdapter.addResult(result.copy(
                        timestamp = (segmentCounter * 5 * 1000).toLong()  // Use segmentCounter
                    ))
                    recyclerView.smoothScrollToPosition(resultAdapter.itemCount - 1)
                }
                frame.recycle()
            }

            frameAnalyzer.reset()
            frameCount = 0
            segmentCounter++  // Increment the counter
        }

        bitmap.recycle()
    }

       private fun ImageProxy.toBitmap(): Bitmap {
        val buffer = planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                finish()
            }
        }
    }
}