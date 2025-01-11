package com.example.firstaidfront.models

data class Test(
    val id: Int,
    val formationName: String,
    val testDate: String,
    val isPassed: Boolean,
    val score: Int,
    val totalQuestions: Int
)