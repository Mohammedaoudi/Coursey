package com.example.firstaidfront.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.databinding.ItemCategoryBinding
import com.example.firstaidfront.models.Category

class CategoryAdapter(private val onItemClick: (Category) -> Unit) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categories = listOf<Category>()
    private var filteredCategories = listOf<Category>()

    fun setCategories(newCategories: List<Category>) {
        categories = newCategories
        filteredCategories = newCategories
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredCategories = if (query.isEmpty()) {
            categories
        } else {
            categories.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredCategories[position])
    }

    override fun getItemCount() = filteredCategories.size

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.apply {
                // Set category name
                categoryName.text = category.name

                // Set category description
                categoryDescription.text = category.description

                // Set lesson count
                lessonCount.text = "${category.trainings.size} Lessons"

                // Show/hide lesson count badge based on training list
                lessonCountBadge.visibility = if (category.trainings.isNotEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Load category icon using Glide
                Glide.with(itemView.context)
                    .load("${ApiClient.BASE_URL}TRAINING-SERVICE/api/images/${category.iconPath}")
                    .into(categoryIcon)

                // Set click listener
                root.setOnClickListener { onItemClick(category) }
            }
        }
    }
}