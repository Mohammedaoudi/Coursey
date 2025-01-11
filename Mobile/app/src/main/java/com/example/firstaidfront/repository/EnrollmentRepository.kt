package com.example.firstaidfront.repository

import android.content.Context
import android.util.Log

import com.example.firstaidfront.api.EnrollmentService

import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.Enrollment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnrollmentRepository(context: Context) {
    private val api = ApiClient.create(EnrollmentService::class.java, context)


    suspend fun isEnrolled(participantId: Int, trainingId: Int): Enrollment? =
        withContext(Dispatchers.IO) {
            try {
                val enrollment = api.checkEnrollmentStatus(participantId, trainingId)
                Log.d("EnrollmentRepo", "Enrollment check response: $enrollment")
                enrollment
            } catch (e: Exception) {
                Log.e("EnrollmentRepo", "Error checking enrollment status", e)
                null
            }
        }

    suspend fun getEnrollmentById(enrollmentId: Int): Enrollment? =
        withContext(Dispatchers.IO) {
            try {
                api.getEnrollmentById(enrollmentId)
            } catch (e: Exception) {
                Log.e("EnrollmentRepo", "Error getting enrollment", e)
                throw e
            }
        }

    suspend fun enrollInTraining(participantId: Int, trainingId: Int): Enrollment? =
        withContext(Dispatchers.IO) {
            try {
                val enrollment = api.enrollInTraining(participantId, trainingId)
                Log.d("EnrollmentRepo", "Enrollment response: $enrollment")
                enrollment
            } catch (e: Exception) {
                Log.e("EnrollmentRepo", "Error enrolling in training", e)
                throw e
            }
        }



}