package com.example.firstaidfront.api

import com.example.firstaidfront.models.Notification
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {
    @GET("NOTIFICATIONS-SERVICE/api/notifications/user/{userId}")
    suspend fun getUserNotifications(
        @Path("userId") userId: Int,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<List<Notification>>

    @PUT("NOTIFICATIONS-SERVICE/api/notifications/{notificationId}/read")
    suspend fun markAsRead(@Path("notificationId") notificationId: Long): Response<Unit>

    @PUT("NOTIFICATIONS-SERVICE/api/notifications/user/{userId}/read-all")
    suspend fun markAllAsRead(@Path("userId") userId: Int): Response<Unit>

    @GET("NOTIFICATIONS-SERVICE/api/notifications/user/{userId}/count")
    suspend fun getUnreadCount(@Path("userId") userId: Int): Response<Map<String, Long>>

    @DELETE("NOTIFICATIONS-SERVICE/api/notifications/{notificationId}")
    suspend fun deleteNotification(@Path("notificationId") notificationId: Long): Response<Unit>

}