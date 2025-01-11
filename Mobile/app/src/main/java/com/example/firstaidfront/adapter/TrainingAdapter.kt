package com.example.firstaidfront.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.databinding.ItemTrainingListBinding
import com.example.firstaidfront.models.DifficultyLevel
import com.example.firstaidfront.models.Training

class TrainingAdapter(
    private val onTrainingClick: (Training) -> Unit
) : RecyclerView.Adapter<TrainingAdapter.ViewHolder>() {

    private var trainings = listOf<Training>()

    fun setTrainings(newTrainings: List<Training>) {
        trainings = newTrainings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrainingListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(trainings[position])
    }

    override fun getItemCount() = trainings.size

    inner class ViewHolder(private val binding: ItemTrainingListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(training: Training) {
            binding.apply {
                trainingTitle.text = training.title
                trainingDescription.text = training.description

                // Set difficulty level badge
                difficultyBadge.apply {
                    text = training.difficultyLevel.toString()
                    setChipBackgroundColorResource(
                        when (training.difficultyLevel) {
                            DifficultyLevel.BEGINNER -> R.color.difficulty_beginner
                            DifficultyLevel.INTERMEDIATE -> R.color.difficulty_intermediate
                            else -> R.color.difficulty_advanced
                        }
                    )
                }

                // Set duration
                val hours = training.estimatedDurationMinutes / 60
                val minutes = training.estimatedDurationMinutes % 60
                duration.text = when {
                    hours > 0 -> "${hours}h ${minutes}m"
                    else -> "${minutes}m"
                }

                // Load training icon using Glide
                training.iconPath?.let { path ->
                    Glide.with(itemView)
                        .load("${ApiClient.BASE_URL}TRAINING-SERVICE/api/images/$path")
//                        .placeholder(R.drawable.ic_healthtest)
//                        .error(R.drawable.ic_healthtest)
                        .into(trainingIcon)
                }

                // Set AR and AI badges visibility
                arBadge.visibility = if (training.supportAR) View.VISIBLE else View.GONE


                // Set click listener using the provided callback
                root.setOnClickListener {
                    onTrainingClick(training)
                }
            }
        }
    }
}