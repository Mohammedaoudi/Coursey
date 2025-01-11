package com.example.firstaidfront.ui.steps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstaidfront.adapter.CourseItemAdapter
import com.example.firstaidfront.databinding.FragmentStep2Binding
import com.example.firstaidfront.models.CourseItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepFragment2 : Fragment() {
    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!
    private lateinit var courseAdapter: CourseItemAdapter
    private var courses: ArrayList<CourseItem>? = null

    companion object {
        fun newInstance(): StepFragment2 {
            return StepFragment2()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get courses from activity intent
        courses = activity?.intent?.getParcelableArrayListExtra("courses")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupDownloadButton()
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseItemAdapter(courses ?: emptyList())
        binding.recyclerViewCourseContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }
    }

    private fun setupDownloadButton() {
        binding.btnDownloadResource.setOnClickListener {
            createAndSharePDF()
        }
    }

    private fun createAndSharePDF() {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val currentDateTime = dateFormat.format(Date())

            // Build content from courses
            val contentBuilder = StringBuilder()
            contentBuilder.append("Course com.example.firstaidfront.models.Content\n\nGenerated on: $currentDateTime\n\n")

            courses?.forEach { course ->
                contentBuilder.append("${course.name}\n")
                contentBuilder.append("${course.description}\n\n")
            }

            // Save to a temporary file
            val file = File(requireContext().cacheDir, "CourseContent.txt")
            file.writeText(contentBuilder.toString())

            // Share the file
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Course com.example.firstaidfront.models.Content"))

        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "No app found to handle the file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error creating resource", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}