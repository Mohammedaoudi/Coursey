package com.example.firstaidfront.models

data class Module(
    val id: Int,
    val title: String,
    val description: String,
    val orderIndex: Int,
    var finished:Boolean,
    val trainingId:Int,
    val contents: List<Content>?
)
