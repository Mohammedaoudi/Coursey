package com.example.firstaidfront.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.firstaidfront.R
import com.example.firstaidfront.models.Notification
import com.example.firstaidfront.models.NotificationType
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private var notifications = mutableListOf<Notification>()

    fun setNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications.toMutableList()
        notifyDataSetChanged()
    }

    fun markAllAsRead() {
        notifications.forEach { it.isRead = true }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }
    fun removeNotification(position: Int) {
        notifications.removeAt(position)
        notifyItemRemoved(position)
    }
    fun getNotificationAt(position: Int): Notification = notifications[position]

    override fun getItemCount() = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val containerView: MaterialCardView = itemView.findViewById(R.id.notificationContainer)
        private val iconView: ImageView = itemView.findViewById(R.id.notificationIcon)
        private val titleView: TextView = itemView.findViewById(R.id.notificationTitle)
        private val messageView: TextView = itemView.findViewById(R.id.notificationMessage)
        private val timeView: TextView = itemView.findViewById(R.id.notificationTime)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(notification: Notification) {
            containerView.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (notification.isRead) R.color.background_light else R.color.unread_background
                )
            )

            // Set icon based on type
            iconView.setImageResource(getIconForType(notification.type))
            iconView.backgroundTintList = ColorStateList.valueOf(getColorForType(notification.type))

            titleView.text = notification.title
            messageView.text = notification.message
            timeView.text = formatTimestamp(notification.timestamp)
            unreadIndicator.isVisible = !notification.isRead
        }

        private fun getIconForType(type: NotificationType): Int = when(type) {
            NotificationType.TRAINING_REMINDER -> R.drawable.ic_test
            NotificationType.TEST_RESULT -> R.drawable.ic_test
            NotificationType.CERTIFICATE_READY -> R.drawable.ic_test
            NotificationType.COURSE_UPDATE -> R.drawable.ic_test
            NotificationType.GENERAL_INFO -> R.drawable.ic_test
        }

        private fun getColorForType(type: NotificationType): Int = when(type) {
            NotificationType.TRAINING_REMINDER -> Color.parseColor("#2196F3") // Blue
            NotificationType.TEST_RESULT -> Color.parseColor("#4CAF50") // Green
            NotificationType.CERTIFICATE_READY -> Color.parseColor("#FFC107") // Gold
            NotificationType.COURSE_UPDATE -> Color.parseColor("#9C27B0") // Purple
            NotificationType.GENERAL_INFO -> Color.parseColor("#607D8B") // Blue Gray
        }

        private fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Just now"
                diff < 3600_000 -> "${diff / 60_000}m ago"
                diff < 86400_000 -> "${diff / 3600_000}h ago"
                else -> SimpleDateFormat("MMM dd", Locale.getDefault())
                    .format(Date(timestamp))
            }
        }
    }
}