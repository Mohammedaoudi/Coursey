package com.example.firstaidfront.api

import com.example.firstaidfront.models.ContentParagraph
import com.example.firstaidfront.models.Module
import com.example.firstaidfront.models.Quiz
import com.example.firstaidfront.models.TestResult
import com.example.firstaidfront.models.TestResultSubmissionDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ModuleService {
    @GET("TRAINING-SERVICE/api/modules/{id}")
    suspend fun getModuleById(@Path("id") moduleId: Int): Module

    @GET("TRAINING-SERVICE/api/modules/contents/{id}/paragraphs")
    suspend fun getContentParagraphs(@Path("id") contentId: Int): List<ContentParagraph>

    @GET("TRAINING-SERVICE/api/modules/{moduleId}/quizzes")
    suspend fun getQuizzesByModuleId(@Path("moduleId") moduleId: Int): List<Quiz>

    @PUT("PARTICIPANT-SERVICE/api/enrollments/{enrollmentId}/modules/{moduleId}/complete")
    suspend fun markModuleComplete(
        @Path("enrollmentId") enrollmentId: Int,
        @Path("moduleId") moduleId: Int
    )


    @POST("PARTICIPANT-SERVICE/api/test-results/submit")
    suspend fun submitTestResult(@Body submission: TestResultSubmissionDTO): TestResult
}