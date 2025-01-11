package com.example.firstaidfront.models

data class TestResultSubmissionDTO(
    val participantId: Int,
    val trainingId: Int,
    val moduleId: Int,
    val answers: List<ParticipantAnswerDTO>
)

// ParticipantAnswerDTO.kt
data class ParticipantAnswerDTO(
    val quizId: Int,
    val selectedAnswerIndex: Int
)