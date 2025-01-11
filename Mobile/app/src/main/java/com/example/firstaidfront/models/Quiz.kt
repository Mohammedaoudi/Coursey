package com.example.firstaidfront.models

import android.os.Parcel
import android.os.Parcelable

data class Quiz(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val moduleId: Int,
    val trainingId: Int?,
    val finalQuiz: Boolean
)

