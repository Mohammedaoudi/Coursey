package com.example.firstaidfront.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.databinding.ItemCourseContentBinding
import com.example.firstaidfront.models.CourseItem

class CourseItemAdapter(private var items: List<CourseItem>) :
    RecyclerView.Adapter<CourseItemAdapter.CourseItemViewHolder>() {

    inner class CourseItemViewHolder(private val binding: ItemCourseContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CourseItem) {
            binding.tvTitle.text = item.name
            binding.tvDescription.text = item.description

            // If urlImage is null, update layout to remove image space
            if (item.urlImage == null) {
                binding.ivItemImage.updateLayoutParams {
                    height = 0
                }
                binding.ivItemImage.visibility = View.GONE
            } else {
                // Reset layout params and visibility for images that exist
                binding.ivItemImage.updateLayoutParams {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                binding.ivItemImage.visibility = View.VISIBLE

                // Load the image
                val fullImageUrl = "${ApiClient.BASE_URL}TRAINING-SERVICE/api/images/${item.urlImage}"
                Glide.with(itemView.context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.ic_healthtest)
                    .error(R.drawable.ic_healthtest)
                    .centerCrop()
                    .into(binding.ivItemImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseItemViewHolder {
        val binding = ItemCourseContentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CourseItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}