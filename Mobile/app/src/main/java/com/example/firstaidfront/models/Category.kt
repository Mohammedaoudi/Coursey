package com.example.firstaidfront.models



data class Category(
    val id: Int,
    val name: String,
    val description: String,
    val iconPath: String,
    val trainings: List<Training>
)
