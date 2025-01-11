package com.example.firstaidfront

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstaidfront.adapter.ModuleAdapter
import com.example.firstaidfront.data.TrainingDetailViewModel
import com.example.firstaidfront.databinding.ActivityTrainingDetailBinding
import com.example.firstaidfront.models.DifficultyLevel
import com.example.firstaidfront.models.Training
import com.example.firstaidfront.ui.cprEstimator.LiveAnalysisActivity
import com.example.firstaidfront.ui.cprEstimator.VideoAnalysisActivity
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.launch

class TrainingDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrainingDetailBinding
    private lateinit var moduleAdapter: ModuleAdapter
    private val viewModel: TrainingDetailViewModel by viewModels {
        TrainingDetailViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trainingId = intent.getIntExtra("training_id", -1)
        if (trainingId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupModulesList()
        setupObservers()

        viewModel.loadTraining(trainingId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupModulesList() {
        moduleAdapter = ModuleAdapter()
        binding.modulesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@TrainingDetailActivity)
            adapter = moduleAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe training details
                launch {
                    viewModel.training.collect { training ->
                        training?.let { updateUI(it) }
                    }
                }

                // Observe enrollment status
                launch {
                    viewModel.enrollmentStatus.collect { enrollment ->
                        Log.d("TrainingDetailActivity", "Enrollment status: $enrollment")
                        updateEnrollButton(enrollment?.id != null)
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collect { error ->
                        error?.let { showError(it) }
                    }
                }
            }
        }
    }

    private fun updateUI(training: Training) {
        Log.d("TrainingDetailActivity", "Loading training: ${training.title}")
        // Set title
        supportActionBar?.title = training.title
        binding.trainingTitle.text = training.title
        binding.trainingDescription.text = training.description

        // Set difficulty chip
        binding.difficultyChip.apply {
            text = training.difficultyLevel.toString()
            setChipBackgroundColorResource(
                when (training.difficultyLevel) {
                    DifficultyLevel.BEGINNER -> R.color.difficulty_beginner
                    DifficultyLevel.INTERMEDIATE -> R.color.difficulty_intermediate
                    else -> R.color.difficulty_advanced
                }
            )
        }
        binding.arSupportCard.apply {
            visibility = if (training.supportAR) View.VISIBLE else View.GONE
            setCardBackgroundColor(getColor(R.color.card_ar_bg))
            setOnClickListener {
                if(training.supportAR) {
                    val intent = Intent(this@TrainingDetailActivity, ArActivity::class.java).apply {
                        putExtra("training_title", training.title)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.aiSupportCard.apply {
            visibility = if (training.supportAI) View.VISIBLE else View.GONE
            setCardBackgroundColor(getColor(R.color.card_ai_bg))
            setOnClickListener {
                showAnalysisOptionsDialog()
            }
        }

        // Set duration
        val hours = training.estimatedDurationMinutes / 60
        val minutes = training.estimatedDurationMinutes % 60
        binding.durationText.text = when {
            hours > 0 -> "${hours}h ${minutes}m"
            else -> "${minutes}m"
        }

        // Set prerequisites and goals
        binding.prerequisitesText.text = training.prerequisites
        binding.goalsText.text = training.goals

        // Setup YouTube player
        setupYouTubePlayer(training.urlYtb)

        // Set instructions
        binding.instructionsChipGroup.removeAllViews()
        training.instructions?.forEach { instruction ->
            val chip = com.google.android.material.chip.Chip(this).apply {
                text = instruction
                isClickable = false
                setChipBackgroundColorResource(R.color.pink_light)
                setTextColor(getColor(R.color.white))
            }
            binding.instructionsChipGroup.addView(chip)
        }

        // Simple date handling
        val createdDate = training.createdAt?.split("T")?.firstOrNull() ?: "N/A"
        binding.creationDateText.text = createdDate

        // Update modules list
        moduleAdapter.submitList(training.modules)
    }

    private fun setupYouTubePlayer(videoUrl: String) {
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = extractYouTubeVideoId(videoUrl)
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })
    }


    private fun showAnalysisOptionsDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Choose Analysis Type")
            .setItems(arrayOf("Live Analysis", "Video Analysis")) { _, which ->
                when (which) {
                    0 -> startLiveAnalysis()
                    1 -> startVideoAnalysis()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun startLiveAnalysis() {
        val intent = Intent(this, LiveAnalysisActivity::class.java).apply {
            putExtra("training_id", viewModel.training.value?.id)
            putExtra("training_title", viewModel.training.value?.title)
        }
        startActivity(intent)
    }

    private fun startVideoAnalysis() {
        val intent = Intent(this, VideoAnalysisActivity::class.java).apply {
            putExtra("training_id", viewModel.training.value?.id)
            putExtra("training_title", viewModel.training.value?.title)
        }
        startActivity(intent)
    }

    private fun extractYouTubeVideoId(videoUrl: String): String {
        return try {
            val uri = Uri.parse(videoUrl)
            uri.getQueryParameter("v") ?: videoUrl.substringAfterLast("/")
        } catch (e: Exception) {
            videoUrl
        }
    }

    private fun updateEnrollButton(isEnrolled: Boolean) {
        binding.enrollButton.apply {
            text = if (isEnrolled) "Continue Learning" else "Enroll Now"
            setBackgroundColor(getColor(if (isEnrolled) R.color.pink_dark else R.color.pink_dark))
            setOnClickListener {
                if (isEnrolled) {
                    // Navigate to LearningModuleActivity with current enrollment ID
                    viewModel.enrollmentStatus.value?.let { enrollment ->
                        startLearningModule(enrollment.id ?: return@let)
                    }
                } else {
                    // Enroll and then navigate
                    viewModel.training.value?.let { training ->
                        lifecycleScope.launch {
                            val enrollment = viewModel.enrollInTraining(training.id)
                            enrollment?.id?.let { enrollmentId ->
                                startLearningModule(enrollmentId)
                            }
                        }
                    }
                }
            }
            isEnabled = true
        }
    }

    private fun startLearningModule(enrollmentId: Int) {
        val intent = Intent(this, LearningModuleActivity::class.java).apply {
            putExtra("enrollment_id", enrollmentId)
        }
        startActivity(intent)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.pink_dark))
            .setTextColor(getColor(R.color.white))
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.youtubePlayerView.release()
    }

    override fun onResume() {
        super.onResume()
        viewModel.training.value?.let { training ->
            viewModel.checkEnrollmentStatus(training.id)
        }
    }
}