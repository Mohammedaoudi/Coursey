package com.example.firstaidfront.repository
import android.content.Context
import android.util.Log


import com.example.firstaidfront.api.ModuleService

import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.ContentParagraph

import com.example.firstaidfront.models.Module
import com.example.firstaidfront.models.ParticipantAnswerDTO
import com.example.firstaidfront.models.Quiz
import com.example.firstaidfront.models.TestResultSubmissionDTO

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModuleRepository(context: Context) {
    private val api = ApiClient.create(ModuleService::class.java, context)

    suspend fun getModuleById(moduleId: Int): Module = withContext(Dispatchers.IO) {
        try {
            api.getModuleById(moduleId)
        } catch (e: Exception) {
            Log.e("ModuleRepo", "Error getting module", e)
            throw e
        }
    }

    suspend fun getContentParagraphs(contentId: Int): List<ContentParagraph> =
        withContext(Dispatchers.IO) {
            try {
                api.getContentParagraphs(contentId)
            } catch (e: Exception) {
                Log.e("ModuleRepo", "Error getting paragraphs", e)
                throw e
            }
        }
    suspend fun getQuizzesByModuleId(moduleId: Int): List<Quiz> = withContext(Dispatchers.IO) {
        try {
            api.getQuizzesByModuleId(moduleId)
        } catch (e: Exception) {
            Log.e("QuizRepo", "Error getting quizzes", e)
            throw e
        }
    }
    suspend fun markModuleComplete(enrollmentId: Int, moduleId: Int) = withContext(Dispatchers.IO) {
        try {
            api.markModuleComplete(enrollmentId, moduleId)
        } catch (e: Exception) {
            Log.e("EnrollmentRepo", "Error marking module complete", e)
            throw e
        }



    }
    // ModuleRepository.kt
    suspend fun submitFinalTest(
        enrollmentId: Int,
        moduleId: Int,
        participantId: Int,
        trainingId: Int,
        userAnswers: Map<Int, Int>
    ) = withContext(Dispatchers.IO) {
        try {
            // Convert user answers to DTO format, preserving the actual selected answers
            val answers = userAnswers.map { (quizId, selectedAnswer) ->
                ParticipantAnswerDTO(
                    quizId = quizId,
                    selectedAnswerIndex = selectedAnswer // Keep the actual selected index
                )
            }

            Log.d("SubmitTest", "Sending answers: $answers") // Debug log

            val submission = TestResultSubmissionDTO(
                participantId = participantId,
                trainingId = trainingId,
                moduleId=moduleId,
                answers = answers
            )

            api.submitTestResult(submission)
        } catch (e: Exception) {
            Log.e("ModuleRepo", "Error submitting final test", e)
            throw e
        }
    }
}