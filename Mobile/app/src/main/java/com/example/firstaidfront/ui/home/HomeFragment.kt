package com.example.firstaidfront.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.firstaidfront.CategoryTrainingsActivity
import com.example.firstaidfront.ChatActivity
import com.example.firstaidfront.R
import com.example.firstaidfront.TrainingActivity
import com.example.firstaidfront.adapter.CategoryAdapter
import com.example.firstaidfront.databinding.FragmentHomeBinding
import com.example.firstaidfront.models.Category
import com.example.firstaidfront.config.TokenManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.Factory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        setupSearch()
        setupChatButton() // Add this line

        setupUserInfo()
        viewModel.loadCategories()
    }

    private fun setupUI() {
        // Setup loading animation
        binding.loadingAnimation.apply {
            setAnimation(R.raw.loading_animation)
            playAnimation()
        }

        // Setup CategoryAdapter with click handler
        categoryAdapter = CategoryAdapter { category ->
            startActivity(Intent(requireContext(), CategoryTrainingsActivity::class.java).apply {
                putExtra("category_id", category.id)
                putExtra("category_name", category.name)
            })
            activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Setup RecyclerView with GridLayoutManager (2 columns)
        binding.categoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = categoryAdapter
            addItemDecoration(GridSpacingItemDecoration(2, 16, true))
        }

        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout?.setOnRefreshListener {
            viewModel.loadCategories()
        }
    }

    private fun setupUserInfo() {
        val fullName = TokenManager.getFullName(requireContext())

        if (fullName.isBlank()) {
            // If name is not available, try to fetch it
            lifecycleScope.launch {
                try {
                    TokenManager.fetchAndSaveParticipantInfo(requireContext())
                    binding.userNameText.text = TokenManager.getFullName(requireContext())
                } catch (e: Exception) {
                    Log.e("HomeFragment", "Error fetching user info", e)
                    binding.userNameText.text = "Welcome"
                }
            }
        } else {
            binding.userNameText.text = fullName
        }

        // Set welcome message based on time of day
        binding.welcomeText.text = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }


    private fun setupChatButton() {
        binding.chatButton.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }
    }




    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                categoryAdapter.filter(s.toString())
            }
        })
    }

    private  fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { categories ->
                        binding.loadingAnimation.apply {
                            cancelAnimation()
                            visibility = View.GONE
                        }
                        binding.categoriesRecyclerView.visibility = View.VISIBLE
                        binding.swipeRefreshLayout?.isRefreshing = false

                        // Add this line to update the adapter with categories
                        categoryAdapter.setCategories(categories)
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        error?.let {
                            binding.swipeRefreshLayout?.isRefreshing = false
                            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                                .setBackgroundTint(resources.getColor(R.color.pink_dark, null))
                                .setTextColor(resources.getColor(R.color.white, null))
                                .show()
                        }
                    }
                }
            }
        }
    }




    private fun navigateToTrainingsList(category: Category) {
        // Add fade animation for transition
        startActivity(
            Intent(requireContext(), TrainingActivity::class.java).apply {
                putExtra("category_id", category.id)
                putExtra("category_name", category.name)
            }
        )
        activity?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.loadingAnimation.cancelAnimation()

        _binding = null
    }

    // Grid spacing decoration class
    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: android.graphics.Rect,
            view: View,
            parent: androidx.recyclerview.widget.RecyclerView,
            state: androidx.recyclerview.widget.RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}