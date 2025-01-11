package com.example.firstaidfront.api

import com.example.firstaidfront.models.Category
import com.example.firstaidfront.models.Training
import retrofit2.http.GET
import retrofit2.http.Path

interface TrainingService {
    @GET("TRAINING-SERVICE/api/trainings/category/{categoryId}")
    suspend fun getTrainingsByCategory(@Path("categoryId") categoryId: Int): List<Training>
    @GET("TRAINING-SERVICE/api/trainings/{id}")
    suspend fun getTraining(@Path("id") trainingId: Int): Training
}