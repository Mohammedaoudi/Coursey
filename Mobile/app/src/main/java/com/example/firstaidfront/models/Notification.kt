package com.example.firstaidfront.models

data class Notification(
    val id: Long,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long,
    var isRead: Boolean = false,
    val actionData: String? = null // For deep linking
)
