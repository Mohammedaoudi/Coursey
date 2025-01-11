package com.example.firstaidfront.ui.notifications

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.models.Notification
import com.example.firstaidfront.repository.NotificationRepository
import com.example.firstaidfront.utils.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel private constructor(
    private val repository: NotificationRepository,
    private val context: Context
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _unreadCount = MutableStateFlow(0L)
    val unreadCount: StateFlow<Long> = _unreadCount.asStateFlow()

    init {
        loadNotifications()
        updateUnreadCount()
    }

    fun refreshNotifications() {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val participantId = TokenManager.getParticipantId(context)
                    ?: throw IllegalStateException("Participant ID not found")

                Log.d("token hna", participantId.toString())
                repository.getUserNotifications(participantId).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            _notifications.value = result.data
                            _error.value = null
                        }
                        is ApiResult.Error -> {
                            _error.value = result.message
                            Log.e("NotificationsVM", "Error loading notifications: ${result.message}")
                        }
                        is ApiResult.Loading -> {
                            _isLoading.value = true
                        }
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("NotificationsVM", "Error loading notifications", e)
                _isLoading.value = false
            }
        }
    }

    fun deleteNotification(notificationId: Long) {
        viewModelScope.launch {
            try {
                Log.d("NotificationsVM", "Starting deletion for notification: $notificationId")
                repository.deleteNotification(notificationId).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            Log.d("NotificationsVM", "Deletion successful for notification: $notificationId")
                            _notifications.value = _notifications.value.filter { it.id != notificationId }
                            updateUnreadCount()
                        }
                        is ApiResult.Error -> {
                            Log.e("NotificationsVM", "Error deleting notification: ${result.message}")
                        }
                        is ApiResult.Loading -> {
                            Log.d("NotificationsVM", "Deletion in progress...")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Exception in deleteNotification", e)
            }
        }
    }

    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            try {
                when (repository.markAsRead(notificationId)) {
                    is ApiResult.Success -> {
                        _notifications.value = _notifications.value.map { notification ->
                            if (notification.id == notificationId) {
                                notification.copy(isRead = true)
                            } else {
                                notification
                            }
                        }
                        updateUnreadCount()
                    }
                    is ApiResult.Error -> {
                        Log.e("NotificationsVM", "Error marking notification as read")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error marking notification as read", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val participantId = TokenManager.getParticipantId(context)
                    ?: throw IllegalStateException("Participant ID not found")

                when (repository.markAllAsRead(participantId)) {
                    is ApiResult.Success -> {
                        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                        _unreadCount.value = 0
                    }
                    is ApiResult.Error -> {
                        Log.e("NotificationsVM", "Error marking all notifications as read")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error marking all notifications as read", e)
            }
        }
    }

    private fun updateUnreadCount() {
        viewModelScope.launch {
            try {
                val participantId = TokenManager.getParticipantId(context)
                    ?: throw IllegalStateException("Participant ID not found")

                repository.getUnreadCount(participantId).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            _unreadCount.value = result.data
                        }
                        is ApiResult.Error -> {
                            Log.e("NotificationsVM", "Error getting unread count: ${result.message}")
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsVM", "Error updating unread count", e)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
                return NotificationsViewModel(NotificationRepository(context), context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}