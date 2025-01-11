package com.example.firstaidfront.models

data class Training(
    val id: Int,
    val title: String,
    val description: String,
    val iconPath: String?,
    val difficultyLevel: DifficultyLevel,
    val estimatedDurationMinutes: Int,
    val goals: String,
    val prerequisites: String,
    val supportAR: Boolean,
    val supportAI: Boolean,
    val urlYtb: String,
    val categoryId: Int,
    val categoryName: String,
    val instructions: List<String>,
    val modules: List<Module>,
    val quizzes: List<Quiz>?,
    val createdAt: String,
    val updatedAt: String,
    val published: Boolean
)