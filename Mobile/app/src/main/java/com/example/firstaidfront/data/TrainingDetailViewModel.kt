package com.example.firstaidfront.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.models.Enrollment
import com.example.firstaidfront.models.Training
import com.example.firstaidfront.repository.EnrollmentRepository
import com.example.firstaidfront.repository.TrainingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class TrainingDetailViewModel(private val context: Context) : ViewModel() {
    private val trainingRepository = TrainingRepository(context)
    private val enrollmentRepository = EnrollmentRepository(context)
    private val participantId = TokenManager.getParticipantId(context)

    private val _training = MutableStateFlow<Training?>(null)
    val training: StateFlow<Training?> = _training.asStateFlow()

    private val _enrollmentStatus = MutableStateFlow<Enrollment?>(null)
    val enrollmentStatus: StateFlow<Enrollment?> = _enrollmentStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> = _isEnrolled.asStateFlow()

    fun loadTraining(trainingId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Load training details
                val trainingDetails = trainingRepository.getTrainingById(trainingId)
                _training.value = trainingDetails

                // Log the training details
                Log.d("TrainingDetailViewModel", "Training details loaded: $trainingDetails")

                // Check enrollment status
                checkEnrollmentStatus(trainingId)
            } catch (e: Exception) {
                _error.value = when (e) {
                    is IOException -> "Network error. Please check your connection."
                    else -> "Error loading training details: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _isEnrolling = MutableStateFlow(false)
    val isEnrolling: StateFlow<Boolean> = _isEnrolling.asStateFlow()

    suspend fun enrollInTraining(trainingId: Int): Enrollment? {
        _isEnrolling.value = true
        return try {
            participantId?.let { pId ->
                val enrollment = enrollmentRepository.enrollInTraining(pId, trainingId)
                _enrollmentStatus.value = enrollment
                _isEnrolled.value = enrollment?.id != null
                enrollment
            }
        } catch (e: Exception) {
            _error.value = "Failed to enroll: ${e.localizedMessage}"
            null
        } finally {
            _isEnrolling.value = false
        }
    }

    fun checkEnrollmentStatus(trainingId: Int) {
        viewModelScope.launch {
            try {
                participantId?.let { pId ->
                    val enrollment = enrollmentRepository.isEnrolled(pId, trainingId)
                    _enrollmentStatus.value = enrollment
                    _isEnrolled.value = enrollment?.id != null
                    Log.d("TrainingDetailViewModel", "Enrollment status: ${_enrollmentStatus.value}")
                }
            } catch (e: Exception) {
                Log.e("TrainingDetailViewModel", "Error checking enrollment status", e)
                _enrollmentStatus.value = null
                _isEnrolled.value = false
                _error.value = "Error checking enrollment status: ${e.localizedMessage}"
            }
        }
    }






    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrainingDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TrainingDetailViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}