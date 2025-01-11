package com.example.firstaidfront.adapter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.example.firstaidfront.R
import com.example.firstaidfront.databinding.ProgresseItemModuleBinding
import com.example.firstaidfront.models.ModuleProgress


class ModuleProgressAdapter : RecyclerView.Adapter<ModuleProgressAdapter.ModuleViewHolder>() {
    private var modules = listOf<ModuleProgress>()
    private var onModuleClick: ((ModuleProgress) -> Unit)? = null

    fun setOnModuleClickListener(listener: (ModuleProgress) -> Unit) {
        onModuleClick = listener
    }

    fun submitList(newModules: List<ModuleProgress>) {
        modules = newModules
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val binding = ProgresseItemModuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ModuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.bind(modules[position])
    }

    override fun getItemCount() = modules.size

    inner class ModuleViewHolder(
        private val binding: ProgresseItemModuleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(module: ModuleProgress) {
            binding.apply {
                moduleNameText.text = module.moduleName
                statusText.text = module.status?.replace("_", " ")

                val isLocked = module.status == "ENROLLED"
                statusIcon.setImageResource(
                    if (isLocked) R.drawable.ic_lock
                    else R.drawable.ic_play_circle
                )

                // Set colors based on status
                val statusColor = if (isLocked) {
                    R.color.gray_light
                } else {
                    R.color.green_success
                }
                statusText.setTextColor(itemView.context.getColor(statusColor))

                // Handle click events
                root.isClickable = !isLocked
                root.isFocusable = !isLocked

                if (!isLocked) {
                    root.setOnClickListener {
                        onModuleClick?.invoke(module)
                    }
                }

                // Add ripple effect only for unlocked modules
                root.foreground = if (isLocked) {
                    null
                } else {
                    val typedValue = TypedValue()
                    itemView.context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
                    ContextCompat.getDrawable(itemView.context, typedValue.resourceId)
                }

            }
        }
    }
}