package com.example.firstaidfront

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
import com.example.firstaidfront.adapter.ContentPagerAdapter
import com.example.firstaidfront.data.ModuleViewModel
import com.example.firstaidfront.databinding.ActivityModuleBinding
import com.example.firstaidfront.models.Module
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ModuleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityModuleBinding
    private lateinit var contentPagerAdapter: ContentPagerAdapter
    private var enrollmentId: Int = -1
    private val viewModel: ModuleViewModel by viewModels {
        ModuleViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModuleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val moduleId = intent.getIntExtra("module_id", -1)
        enrollmentId = intent.getIntExtra("enrollment_id", -1)
        if (moduleId == -1 || enrollmentId == -1) {  // Updated check
            finish()
            return
        }

        setupToolbar()
        setupViewPager()
        setupObservers()

        viewModel.loadModule(moduleId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupViewPager() {
        contentPagerAdapter = ContentPagerAdapter(this,enrollmentId)
        binding.viewPager.isUserInputEnabled = false // Disable swiping until data is loaded
        binding.viewPager.adapter = contentPagerAdapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.module.collect { module ->
                        module?.let { updateUI(it) }
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

    private fun updateUI(module: Module) {
        supportActionBar?.title = module.title

        // Update ViewPager with contents + quiz
        val contents = module.contents?.toMutableList() ?: mutableListOf()
        contentPagerAdapter.updateContents(contents, module.id)

        // Enable ViewPager swiping after data is loaded
        binding.viewPager.isUserInputEnabled = true

        // Setup tab layout after data is loaded
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = contentPagerAdapter.getTabTitle(position)
        }.attach()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}