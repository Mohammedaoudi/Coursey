package com.example.firstaidfront.repository

import android.content.Context
import android.util.Log
import com.example.firstaidfront.api.CategoryService
import com.example.firstaidfront.api.TrainingService

import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.Category
import com.example.firstaidfront.models.Training
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TrainingRepository(context: Context) {
    private val api = ApiClient.create(TrainingService::class.java, context)

    suspend fun getTrainingsByCategory(categoryId: Int): List<Training> = withContext(Dispatchers.IO) {
        try {
            val trainings = api.getTrainingsByCategory(categoryId)
            Log.d("TrainingRepository", "Retrieved ${trainings.size} trainings for category $categoryId")
            trainings
        } catch (e: Exception) {
            Log.e("TrainingRepository", "Error fetching trainings for category $categoryId", e)
            throw e
        }
    }

    suspend fun getTrainingById(trainingId: Int): Training = withContext(Dispatchers.IO) {
        try {
            val training = api.getTraining(trainingId)
            Log.d("TrainingRepo", "Retrieved training details for ID $trainingId")
            training
        } catch (e: Exception) {
            Log.e("TrainingRepo", "Error fetching training details", e)
            throw e
        }
    }



}