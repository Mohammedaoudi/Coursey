package com.example.firstaidfront

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstaidfront.adapter.ModuleProgressAdapter
import com.example.firstaidfront.data.LearningModuleViewModel

import com.example.firstaidfront.databinding.ActivityLearningModuleBinding
import com.example.firstaidfront.models.Enrollment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LearningModuleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLearningModuleBinding
    private lateinit var moduleAdapter: ModuleProgressAdapter
    private val viewModel: LearningModuleViewModel by viewModels {
        LearningModuleViewModel.Factory(applicationContext)
    }

    private var currentEnrollmentId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearningModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val enrollmentId = intent.getIntExtra("enrollment_id", -1)
        currentEnrollmentId = enrollmentId
        if (enrollmentId == -1) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        loadData()

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Module Progress"
        }
    }

    private fun loadData() {
        viewModel.loadEnrollment(currentEnrollmentId )
    }
    override fun onResume() {
        super.onResume()
        loadData()  // Reload data when activity resumes
    }

    private fun setupRecyclerView() {
        moduleAdapter = ModuleProgressAdapter().apply {
            setOnModuleClickListener { moduleProgress ->
                if (moduleProgress.status == "IN_PROGRESS") {
                    navigateToModule(moduleProgress.moduleId)
                }
            }
        }

        binding.modulesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@LearningModuleActivity)
            adapter = moduleAdapter
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 250
                removeDuration = 250
            }
        }
    }

    private fun navigateToModule(moduleId: Int?) {
        moduleId?.let {
            val intent = Intent(this, ModuleActivity::class.java).apply {
                putExtra("module_id", moduleId)
                putExtra("enrollment_id", viewModel.enrollment.value?.id)  // Add enrollment ID
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }

            // Start activity with custom transition
            startActivity(intent)
            overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.enrollment.collect { enrollment ->
                        enrollment?.let { updateUI(it) }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        binding.progressBar.visibility =
                            if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let { showError(it) }
                    }
                }
            }
        }
    }
    private fun updateUI(enrollment: Enrollment) {
        moduleAdapter.submitList(enrollment.moduleProgresses)
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}