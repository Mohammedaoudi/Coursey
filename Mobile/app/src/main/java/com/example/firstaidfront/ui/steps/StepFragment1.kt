package com.example.firstaidfront.ui.steps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.firstaidfront.R
import com.example.firstaidfront.databinding.FragmentStep1Binding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class StepFragment1 : Fragment() {
    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!
    private var stepNumber: Int = 1
    private var trainingName: String? = null
    private var videoUrl: String? = null
    private var goals: String? = null
    private var instructions: ArrayList<String>? = null


    companion object {
        private const val ARG_STEP_NUMBER = "step_number"

        fun newInstance(stepNumber: Int): StepFragment1 {
            val fragment = StepFragment1()
            val args = Bundle().apply {
                putInt(ARG_STEP_NUMBER, stepNumber)
            }
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepNumber = arguments?.getInt(ARG_STEP_NUMBER, 1) ?: 1
        activity?.intent?.let { intent ->
            trainingName = intent.getStringExtra("training_name")
            videoUrl = intent.getStringExtra("video_url")
            goals = intent.getStringExtra("goals")
            instructions = intent.getStringArrayListExtra("instructions")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup video player with the video ID directly
        setupYouTubePlayer(videoUrl ?: getDefaultVideoIdForStep())
        lifecycle.addObserver(binding.youtubePlayerView)

        // Setup goals text
        binding.goalsDescription.text = goals ?: getString(R.string.default_goals_description)

        // Setup instructions
        setupInstructions()

        // Show AR card only for CPR training
        if (trainingName == "CPR") {
            binding.arCard.visibility = View.VISIBLE
            setupArCard()
        } else {
            binding.arCard.visibility = View.GONE
        }
    }

    private fun setupInstructions() {
        // Clear existing instruction views
        binding.instructionsContainer.removeAllViews()

        // Add each instruction
        instructions?.forEachIndexed { index, instruction ->
            val instructionLayout = layoutInflater.inflate(R.layout.item_instruction, binding.instructionsContainer, false)

            // Find views in the inflated layout
            val numberText = instructionLayout.findViewById<TextView>(R.id.instructionNumber)
            val instructionText = instructionLayout.findViewById<TextView>(R.id.instructionText)

            // Set the instruction number and text
            numberText.text = (index + 1).toString()
            instructionText.text = instruction

            // Add the instruction view to the container
            binding.instructionsContainer.addView(instructionLayout)
        }
    }



    private fun getDefaultVideoIdForStep(): String {
        return when (trainingName) {
            "CPR" -> "MKZclIAJV_A"
            "First Aid" -> "xfFf3-8sRAA"
            "Emergency" -> "different_video_id"
            else -> "xfFf3-8sRAA"
        }
    }



    private fun setupArCard() {
        binding.arCard.setOnClickListener {
            // Handle AR feature launch here
            // For example, launch AR activity or show AR dialog
        }
    }



    private fun setupYouTubePlayer(videoId: String) {
        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}