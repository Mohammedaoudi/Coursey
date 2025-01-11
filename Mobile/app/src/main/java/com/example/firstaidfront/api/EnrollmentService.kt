package com.example.firstaidfront.api


import com.example.firstaidfront.models.Enrollment
import retrofit2.http.*

interface EnrollmentService {
    @GET("PARTICIPANT-SERVICE/api/enrollments/check")
    suspend fun checkEnrollmentStatus(
        @Query("participantId") participantId: Int,
        @Query("trainingId") trainingId: Int
    ): Enrollment

    @POST("PARTICIPANT-SERVICE/api/enrollments/enroll")
    suspend fun enrollInTraining(
        @Query("participantId") participantId: Int,
        @Query("trainingId") trainingId: Int
    ): Enrollment

    @GET("PARTICIPANT-SERVICE/api/enrollments/{id}")
    suspend fun getEnrollmentById(@Path("id") enrollmentId: Int): Enrollment






}