package com.example.firstaidfront.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firstaidfront.models.Content
import com.example.firstaidfront.ui.ContentFragment
import com.example.firstaidfront.ui.QuizFragment

class ContentPagerAdapter(activity: FragmentActivity,private val enrollmentId: Int) :
    FragmentStateAdapter(activity) {

    private var contents: List<Content> = emptyList()
    private var moduleId: Int? = null

    fun updateContents(newContents: List<Content>, newModuleId: Int) {
        // Add these lines to ensure we have valid data before notifying changes
        if (newModuleId <= 0) {
            throw IllegalArgumentException("Invalid module ID: $newModuleId")
        }
        contents = newContents
        moduleId = newModuleId
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        // Only add quiz tab if we have a valid moduleId
        return if (moduleId != null) contents.size + 1 else contents.size
    }

    override fun createFragment(position: Int): Fragment {
        return if (position < contents.size) {
            ContentFragment.newInstance(contents[position])
        } else {
            requireNotNull(moduleId) { "Module ID is required for quiz fragment" }
            QuizFragment.newInstance(moduleId!!, enrollmentId)  // Pass enrollmentId
        }
    }

    fun getTabTitle(position: Int): String {
        return if (position < contents.size) {
            "Part ${position + 1}"
        } else {
            "Quiz"
        }
    }
}