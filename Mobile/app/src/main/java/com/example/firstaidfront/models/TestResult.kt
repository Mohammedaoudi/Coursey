package com.example.firstaidfront.models



data class TestResult(
    val id:Int,
    val participantId: Int,
    val trainingId: Int,
    val userAnswers: List<ParticipantAnswer>,
    val score: Float,
    val submissionDate: String,
    val passed: Boolean,
    val trainingDTO: TrainingDTO
) {
    data class TrainingDTO(
        val id: Int,
        val title: String,
        val description: String,
        val instructions: List<String>,
        val quizzes: List<Any>? = null,
        val estimatedDurationMinutes :Int
    )
}
