package com.example.firstaidfront

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstaidfront.adapter.TrainingAdapter
import com.example.firstaidfront.data.CategoryTrainingsViewModel
import com.example.firstaidfront.databinding.ActivityCategoryTrainingsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CategoryTrainingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryTrainingsBinding
    private lateinit var trainingAdapter: TrainingAdapter
    private val viewModel: CategoryTrainingsViewModel by viewModels {
        CategoryTrainingsViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryTrainingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getIntExtra("category_id", -1)
        val categoryName = intent.getStringExtra("category_name") ?: "Trainings"

        setupToolbar(categoryName)
        setupRecyclerView()
        setupObservers()

        viewModel.loadTrainings(categoryId)
    }

    private fun setupToolbar(categoryName: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = categoryName
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupRecyclerView() {
        trainingAdapter = TrainingAdapter { training ->
            // Navigate to training detail
            startActivity(Intent(this, TrainingDetailActivity::class.java).apply {
                putExtra("training_id", training.id)
                putExtra("training_name", training.title)
            })
        }

        binding.trainingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CategoryTrainingsActivity)
            adapter = trainingAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshTrainings()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.trainings.collect { trainings ->
                        binding.loadingAnimation.visibility = View.GONE
                        binding.trainingsRecyclerView.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false
                        trainingAdapter.setTrainings(trainings)
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            binding.swipeRefreshLayout.isRefreshing = false
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(getColor(R.color.pink_dark))
                                .setTextColor(getColor(R.color.white))
                                .show()
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.loadingAnimation.visibility =
                            if (isLoading && trainingAdapter.itemCount == 0) View.VISIBLE
                            else View.GONE
                        binding.trainingsRecyclerView.visibility =
                            if (isLoading && trainingAdapter.itemCount == 0) View.GONE
                            else View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}