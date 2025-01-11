package com.example.firstaidfront.repository

import android.content.Context
import android.util.Log
import com.example.firstaidfront.api.NotificationService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.Notification
import com.example.firstaidfront.utils.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException


class NotificationRepository(private val context: Context) {
    private val notificationApi: NotificationService = ApiClient.create(NotificationService::class.java, context)

    fun getUserNotifications(userId: Int): Flow<ApiResult<List<Notification>>> = flow {
        emit(ApiResult.Loading())
        try {
            val response = notificationApi.getUserNotifications(userId)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(ApiResult.Success(it))
                } ?: emit(ApiResult.Error("Empty response body"))
            } else {
                emit(ApiResult.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: IOException) {
            emit(ApiResult.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(ApiResult.Error("Unexpected error: ${e.message}"))
        }
    }

    suspend fun markAsRead(notificationId: Long): ApiResult<Unit> {
        return try {
            val response = notificationApi.markAsRead(notificationId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error marking notification as read: ${e.message}")
        }
    }

    suspend fun markAllAsRead(userId: Int): ApiResult<Unit> {
        return try {
            val response = notificationApi.markAllAsRead(userId)
            if (response.isSuccessful) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error("Error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Error marking all notifications as read: ${e.message}")
        }
    }

    fun deleteNotification(notificationId: Long): Flow<ApiResult<Unit>> = flow {
        emit(ApiResult.Loading())
        Log.d("NotificationsVM2", "Starting deletion for notification: $notificationId")

        try {
            Log.d("NotificationRepo", "Attempting to delete notification: $notificationId")
            val response = notificationApi.deleteNotification(notificationId)
            if (response.isSuccessful) {
                Log.d("NotificationRepo", "Successfully deleted notification: $notificationId")
                emit(ApiResult.Success(Unit))
            } else {
                Log.e("NotificationRepo", "Error deleting notification: ${response.code()}")
                emit(ApiResult.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Exception deleting notification", e)
            emit(ApiResult.Error(e.message ?: "Unknown error"))
        }
    }


    fun getUnreadCount(userId: Int): Flow<ApiResult<Long>> = flow {
        emit(ApiResult.Loading())
        try {
            val response = notificationApi.getUnreadCount(userId)
            if (response.isSuccessful) {
                response.body()?.let { map ->
                    val unreadCount = map["unreadCount"] ?: 0L
                    emit(ApiResult.Success(unreadCount))
                } ?: emit(ApiResult.Error("Empty response body"))
            } else {
                emit(ApiResult.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Error("Error getting unread count: ${e.message}"))
        }
    }
}