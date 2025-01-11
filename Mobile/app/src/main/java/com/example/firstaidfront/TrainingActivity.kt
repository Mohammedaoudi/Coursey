package com.example.firstaidfront

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.firstaidfront.databinding.ActivityTrainingBinding
import com.example.firstaidfront.ui.steps.StepFragment1
import com.example.firstaidfront.ui.steps.StepFragment2
import com.example.firstaidfront.ui.steps.StepFragment3

class TrainingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrainingBinding
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ViewPager
        viewPager = binding.viewPager
        viewPager.adapter = TrainingPagerAdapter(this)

        // Add page change listener to update progress steps
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateProgressSteps(position)
            }
        })

        // Setup back button
        binding.backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateProgressSteps(currentStep: Int) {
        val progressViews = listOf(
            binding.step1Progress,
            binding.step2Progress,
            binding.step3Progress
        )

        progressViews.forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index <= currentStep)
                    R.drawable.progress_step_active
                else
                    R.drawable.progress_step_background
            )
        }
    }

    // Adapter for ViewPager
    private inner class TrainingPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> StepFragment1.newInstance(1)
                1 -> StepFragment2.newInstance()
                2 -> StepFragment3.newInstance()
                else -> StepFragment1.newInstance(1)
            }
        }
    }

    // Override onBackPressed to provide custom back navigation
    override fun onBackPressed() {
        // If not on the first page, move to previous page
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        } else {
            // If on first page, finish the activity
            super.onBackPressed()
        }
    }
}