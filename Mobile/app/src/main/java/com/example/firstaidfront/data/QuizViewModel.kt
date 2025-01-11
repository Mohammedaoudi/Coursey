package com.example.firstaidfront.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.models.Module
import com.example.firstaidfront.models.Quiz
import com.example.firstaidfront.models.QuizResult
import com.example.firstaidfront.models.TestResult
import com.example.firstaidfront.repository.ModuleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuizViewModel private constructor(
    private val repository: ModuleRepository,
    private val context: Context // Add context parameter
) : ViewModel() {
    private val _module = MutableStateFlow<Module?>(null)
    val module: StateFlow<Module?> = _module.asStateFlow()

    private val _quizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()

    private val _moduleCompleted = MutableStateFlow(false)
    val moduleCompleted: StateFlow<Boolean> = _moduleCompleted.asStateFlow()

    private val _testResult = MutableStateFlow<TestResult?>(null)
    val testResult: StateFlow<TestResult?> = _testResult.asStateFlow()

    private val _userAnswers = mutableMapOf<Int, Int>()
    val userAnswers: Map<Int, Int>
        get() = _userAnswers.toMap()

    fun loadModule(moduleId: Int) {
        viewModelScope.launch {
            try {
                val fetchedModule = repository.getModuleById(moduleId)
                _module.value = fetchedModule
                Log.e("Moduleee", "$fetchedModule")

            } catch (e: Exception) {
                Log.e("QuizVM", "Error loading module", e)
            }
        }
    }

    fun loadQuizzes(moduleId: Int) {
        viewModelScope.launch {
            try {
                val fetchedQuizzes = repository.getQuizzesByModuleId(moduleId)
                _quizzes.value = fetchedQuizzes
            } catch (e: Exception) {
                Log.e("QuizVM", "Error loading quizzes", e)
            }
        }
    }

    fun submitAnswer(quizId: Int, selectedAnswerIndex: Int) {
        _userAnswers[quizId] = selectedAnswerIndex
    }

    fun calculateResult() {
        val quizList = _quizzes.value
        var correctAnswers = 0

        quizList.forEach { quiz ->
            val userAnswer = _userAnswers[quiz.id]
            if (userAnswer == quiz.correctAnswerIndex) {
                correctAnswers++
            }
        }

        _quizResult.value = QuizResult(
            totalQuestions = quizList.size,
            correctAnswers = correctAnswers,
            score = (correctAnswers.toFloat() / quizList.size)
        )
    }

    fun markModuleComplete(enrollmentId: Int, moduleId: Int) {
        viewModelScope.launch {
            try {
                repository.markModuleComplete(enrollmentId, moduleId)
                _moduleCompleted.value = true
            } catch (e: Exception) {
                Log.e("QuizVM", "Error marking module complete", e)
            }
        }
    }

    fun submitFinalTest(enrollmentId: Int, moduleId: Int) {
        viewModelScope.launch {
            try {
                val participantId = TokenManager.getParticipantId(context)
                    ?: throw IllegalStateException("Participant ID not found")
                val currentModule = _module.value
                    ?: throw IllegalStateException("Module not found")
                val trainingId = currentModule.trainingId

                // Validate answers before submission
                if (_userAnswers.isEmpty()) {
                    Log.e("QuizVM", "No answers to submit")
                    return@launch
                }

                // Debug logs
                Log.d("QuizVM", "Submitting answers: $_userAnswers")

                val result = repository.submitFinalTest(
                    enrollmentId = enrollmentId,
                    moduleId = moduleId,
                    participantId = participantId,
                    trainingId = trainingId,
                    userAnswers = _userAnswers.toMap() // Create a clean copy of the map
                )

                Log.d("QuizVM", "Submission result: $result")

                _testResult.value = result
                markModuleComplete(enrollmentId, moduleId)
            } catch (e: Exception) {
                Log.e("QuizVM", "Error submitting final test", e)
                // Handle error appropriately
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                return QuizViewModel(ModuleRepository(context), context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}