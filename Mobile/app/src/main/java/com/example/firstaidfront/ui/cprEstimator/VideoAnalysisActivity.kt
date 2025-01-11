package com.example.firstaidfront.ui.cprEstimator

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstaidfront.R
import com.example.firstaidfront.adapter.CPRResultAdapter
import com.example.firstaidfront.cprDL.CPRPredictor
import com.example.firstaidfront.cprDL.CPRResult
import com.example.firstaidfront.cprDL.FrameAnalyzer
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar



class VideoAnalysisActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectButton: MaterialButton
    private lateinit var cprPredictor: CPRPredictor
    private lateinit var frameAnalyzer: FrameAnalyzer
    private lateinit var resultAdapter: CPRResultAdapter

    private var mediaPlayer: MediaPlayer? = null
    private var isAnalyzing = false
    private var currentSegment = 0
    private val segmentFrames = listOf(0, 145, 290, 435, 580, 725)
    private val frameBuffer = mutableListOf<Bitmap>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_analysis)

        initializeViews()
        setupListeners()

        cprPredictor = CPRPredictor(this)
        frameAnalyzer = FrameAnalyzer()
    }

    private fun initializeViews() {
        textureView = findViewById(R.id.textureView)
        recyclerView = findViewById(R.id.recyclerView)
        selectButton = findViewById(R.id.selectButton)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup RecyclerView
        resultAdapter = CPRResultAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VideoAnalysisActivity)
            adapter = resultAdapter
        }

        setupTextureView()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun setupTextureView() {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d("VideoAnalysisss", "Surface texture available: $width x $height")
                mediaPlayer?.setSurface(Surface(surface))
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                Log.d("VideoAnalysisss", "Surface texture size changed: $width x $height")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.d("VideoAnalysisss", "Surface texture destroyed")
                releaseMediaPlayer()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                if (isAnalyzing && mediaPlayer?.isPlaying == true) {
                    processCurrentFrame()
                }
            }
        }
    }

    private fun processCurrentFrame() {
        val framePosition = (mediaPlayer?.currentPosition?.toFloat() ?: 0f) * 29 / 1000

        if (currentSegment < segmentFrames.size - 1) {
            // Check if we're in the current segment
            if (framePosition >= segmentFrames[currentSegment] && framePosition <= segmentFrames[currentSegment + 1]) {
                textureView.bitmap?.let { bitmap ->
                    frameAnalyzer.addFrame(bitmap)
                    Log.d("VideoAnalysisss", "Added frame at position $framePosition for segment $currentSegment")
                }
            }

            // When we reach the end of segment, process it
            if (framePosition >= segmentFrames[currentSegment + 1]) {
                Log.d("VideoAnalysisss", "End of segment $currentSegment reached, processing...")

                val criticalFrame = frameAnalyzer.getCriticalFrame()
                val compressionCount = frameAnalyzer.getCompressionCount()

                criticalFrame?.let { frame ->
                    val result = cprPredictor.analyze(frame, compressionCount)
                    runOnUiThread {
                        resultAdapter.addResult(result.copy(
                            timestamp = (currentSegment * 5 * 1000).toLong()
                        ))
                        recyclerView.smoothScrollToPosition(resultAdapter.itemCount - 1)
                    }
                    frame.recycle()
                }

                frameAnalyzer.reset()
                currentSegment++
            }
        }
    }

    private fun setupListeners() {
        selectButton.setOnClickListener {
            selectVideo()
        }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
        }
        startActivityForResult(intent, VIDEO_SELECT_REQUEST)
    }

    private fun startVideoPlayback(uri: Uri) {
        try {
            releaseMediaPlayer()
            frameAnalyzer.reset()
            resultAdapter.clear()
            currentSegment = 0
            frameBuffer.clear()

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)

                if (textureView.isAvailable) {
                    setSurface(Surface(textureView.surfaceTexture))
                }

                setOnPreparedListener { mp ->
                    val videoRatio = mp.videoWidth.toFloat() / mp.videoHeight.toFloat()
                    val width = textureView.width
                    val height = (width / videoRatio).toInt()

                    Log.d("VideoAnalysisss", "Video size: ${mp.videoWidth} x ${mp.videoHeight}")
                    Log.d("VideoAnalysisss", "TextureView size: $width x $height")

                    textureView.layoutParams = textureView.layoutParams.apply {
                        this.height = height
                    }

                    isAnalyzing = true
                    mp.start()
                    Log.d("VideoAnalysisss", "Video playback started")
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("VideoAnalysisss", "MediaPlayer Error: $what, $extra")
                    false
                }

                setOnCompletionListener {
                    isAnalyzing = false
                    Log.d("VideoAnalysisss", "Video playback completed")
                    runOnUiThread {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Analysis completed",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("VideoAnalysisss", "Error starting video: ${e.message}", e)
            Snackbar.make(
                findViewById(android.R.id.content),
                "Error loading video: ${e.message}",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        isAnalyzing = false
        frameBuffer.forEach { it.recycle() }
        frameBuffer.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_SELECT_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                startVideoPlayback(uri)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    companion object {
        private const val VIDEO_SELECT_REQUEST = 1001
    }
}