package com.example.firstaidfront.data

import android.content.Context
import android.net.http.HttpException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstaidfront.models.DifficultyLevel
import com.example.firstaidfront.models.Training
import com.example.firstaidfront.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

// CategoryTrainingsViewModel.kt
class CategoryTrainingsViewModel(context: Context) : ViewModel() {
    private val repository = TrainingRepository(context)

    private val _trainings = MutableStateFlow<List<Training>>(emptyList())
    val trainings: StateFlow<List<Training>> = _trainings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentCategoryId: Int? = null

    fun loadTrainings(categoryId: Int) {
        if (categoryId == -1) {
            _error.value = "Invalid category ID"
            return
        }

        currentCategoryId = categoryId
        refreshTrainings()
    }

    fun refreshTrainings() {
        currentCategoryId?.let { categoryId ->
            viewModelScope.launch {
                _isLoading.value = true
                _error.value = null // Reset error state

                try {
                    val result = repository.getTrainingsByCategory(categoryId)
                    _trainings.value = result.sortedBy { it.title } // Sort by title
                    if (result.isEmpty()) {
                        _error.value = "No trainings found for this category"
                    }
                } catch (e: Exception) {
                    Log.e("CategoryTrainingsVM", "Error loading trainings", e)
                    _error.value = when (e) {
                        is IOException -> "Network error. Please check your connection."
                        else -> "Error loading trainings: ${e.localizedMessage}"
                    }
                    _trainings.value = emptyList() // Clear current trainings on error
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun getSelectedCategoryId(): Int? = currentCategoryId

    fun filterTrainings(query: String) {
        viewModelScope.launch {
            val currentTrainings = _trainings.value
            if (query.isEmpty()) {
                _trainings.value = currentTrainings
            } else {
                _trainings.value = currentTrainings.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun filterByDifficulty(level: DifficultyLevel?) {
        viewModelScope.launch {
            val currentTrainings = _trainings.value
            if (level == null) {
                _trainings.value = currentTrainings
            } else {
                _trainings.value = currentTrainings.filter {
                    it.difficultyLevel == level
                }
            }
        }
    }

    fun filterByDuration(maxMinutes: Int?) {
        viewModelScope.launch {
            val currentTrainings = _trainings.value
            if (maxMinutes == null) {
                _trainings.value = currentTrainings
            } else {
                _trainings.value = currentTrainings.filter {
                    it.estimatedDurationMinutes <= maxMinutes
                }
            }
        }
    }

    fun filterBySupport(requireAR: Boolean = false, requireAI: Boolean = false) {
        viewModelScope.launch {
            val currentTrainings = _trainings.value
            _trainings.value = currentTrainings.filter { training ->
                (!requireAR || training.supportAR) && (!requireAI || training.supportAI)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryTrainingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CategoryTrainingsViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        private const val TAG = "CategoryTrainingsVM"
    }
}