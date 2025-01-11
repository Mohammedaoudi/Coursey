package com.example.firstaidfront.data
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.models.Enrollment
import com.example.firstaidfront.models.Module
import com.example.firstaidfront.models.Training
import com.example.firstaidfront.repository.EnrollmentRepository
import com.example.firstaidfront.repository.ModuleRepository
import com.example.firstaidfront.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class ModuleViewModel(private val context: Context) : ViewModel() {
    private val moduleRepository = ModuleRepository(context)

    private val _module = MutableStateFlow<Module?>(null)
    val module: StateFlow<Module?> = _module.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadModule(moduleId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = moduleRepository.getModuleById(moduleId)
                _module.value = result
            } catch (e: Exception) {
                _error.value = "Failed to load module: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ModuleViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}