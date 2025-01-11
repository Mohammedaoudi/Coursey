package com.example.firstaidfront.ui.notifications

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firstaidfront.R
import com.example.firstaidfront.adapter.NotificationAdapter
import com.example.firstaidfront.databinding.FragmentNotificationsBinding
import com.example.firstaidfront.models.Notification
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {
    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var notificationAdapter: NotificationAdapter
    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModel.Factory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeNotifications()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.markAllAsRead()
                notificationAdapter.markAllAsRead()
            }
        }
    }


    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            // Add swipe callback
            val swipeCallback = object : ItemTouchHelper.SimpleCallback(
                0, // Drag direction - none
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // Swipe directions
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    val notification = notificationAdapter.getNotificationAt(position)

                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            Log.d("NotificationsFragment", "Swiping to delete notification: ${notification.id}")
                            viewModel.deleteNotification(notification.id)
                            notificationAdapter.removeNotification(position)
                        }
                        ItemTouchHelper.RIGHT -> {
                            Log.d("NotificationsFragment", "Swiping to mark as read: ${notification.id}")
                            viewModel.markAsRead(notification.id)
                            notificationAdapter.notifyItemChanged(position)
                        }
                    }
                }
                override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val isCanceled = dX == 0f && !isCurrentlyActive

                    if (isCanceled) {
                        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        return
                    }

                    // Draw background
                    val background = Paint()
                    val icon: Int
                    val iconTint: Int

                    when {
                        dX > 0 -> { // Swipe right - mark as read
                            background.color = ContextCompat.getColor(requireContext(), R.color.green)
                            icon = R.drawable.ic_arrow_icon
                            iconTint = Color.WHITE
                        }
                        dX < 0 -> { // Swipe left - delete
                            background.color = ContextCompat.getColor(requireContext(), R.color.red_fail)
                            icon = R.drawable.ic_delete
                            iconTint = Color.WHITE
                        }
                        else -> {
                            background.color = Color.TRANSPARENT
                            icon = 0
                            iconTint = Color.TRANSPARENT
                        }
                    }

                    // Draw background
                    val backgroundCornerRadius = 16f
                    val rect = RectF(
                        itemView.left.toFloat(),
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    canvas.drawRoundRect(rect, backgroundCornerRadius, backgroundCornerRadius, background)

                    // Draw icon
                    if (icon != 0) {
                        val iconSize = height * 0.4f
                        val drawable = ContextCompat.getDrawable(requireContext(), icon)
                        drawable?.let {
                            val iconMargin = (height - iconSize) / 2
                            val iconTop = itemView.top + iconMargin
                            val iconBottom = iconTop + iconSize

                            if (dX > 0) { // Swipe right
                                val iconLeft = itemView.left + iconMargin
                                val iconRight = itemView.left + iconMargin + iconSize
                                drawable.setBounds(
                                    iconLeft.toInt(),
                                    iconTop.toInt(),
                                    iconRight.toInt(),
                                    iconBottom.toInt()
                                )
                            } else { // Swipe left
                                val iconLeft = itemView.right - iconMargin - iconSize
                                val iconRight = itemView.right - iconMargin
                                drawable.setBounds(
                                    iconLeft.toInt(),
                                    iconTop.toInt(),
                                    iconRight.toInt(),
                                    iconBottom.toInt()
                                )
                            }

                            drawable.setTint(iconTint)
                            drawable.draw(canvas)
                        }
                    }

                    super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }

            ItemTouchHelper(swipeCallback).attachToRecyclerView(this)
        }
    }
    private fun observeNotifications() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collect { notifications ->
                binding.emptyState.isVisible = notifications.isEmpty()
                notificationAdapter.setNotifications(notifications)
                updateUnreadCount(notifications)
            }
        }
    }

    private fun updateUnreadCount(notifications: List<Notification>) {
        val unreadCount = notifications.count { !it.isRead }
        binding.unreadCountText.text = "$unreadCount unread notifications"
        binding.unreadCountText.isVisible = unreadCount > 0
    }
}