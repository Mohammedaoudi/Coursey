package com.example.firstaidfront.models

import java.time.LocalDateTime

data class Enrollment(
    val id: Int?,
    val participantId: Int,
    val trainingId: Int,
    val enrollmentDate: String?,
    val status: String?,
    val moduleProgresses: List<ModuleProgress> = emptyList()
)


