package com.example.firstaidfront.models

data class QuizResult(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val score: Float
) {
    val percentage: Float = (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100
}