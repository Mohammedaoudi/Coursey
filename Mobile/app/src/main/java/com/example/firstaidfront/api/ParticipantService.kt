package com.example.firstaidfront.api


import com.example.firstaidfront.models.Participant
import com.example.firstaidfront.models.ParticipantAnswer
import com.example.firstaidfront.models.TestResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ParticipantService {
    @GET("PARTICIPANT-SERVICE/api/participants/user/{userId}")
    suspend fun getParticipant(@Path("userId") userId: String): Participant
    @POST("PARTICIPANT-SERVICE/participants/{participantId}/trainings/{trainingId}/submit-test")
    suspend fun submitTest(
        @Path("participantId") participantId: Int,
        @Path("trainingId") trainingId: Int,
        @Body answers: List<ParticipantAnswer>
    ): TestResult


    @GET("PARTICIPANT-SERVICE/api/test-results/participant/{participantId}")
    suspend fun getTestResultsByParticipantId(@Path("participantId") participantId: Int): List<TestResult>

    @GET("PARTICIPANT-SERVICE/api/participants/{id}")
    suspend fun getParticipantById(@Path("id") id: Int): Participant




}