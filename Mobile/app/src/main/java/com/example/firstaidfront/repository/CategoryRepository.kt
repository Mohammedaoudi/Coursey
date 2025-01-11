package com.example.firstaidfront.repository

import android.content.Context
import com.example.firstaidfront.api.CategoryService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(context: Context) {
    private val api = ApiClient.create(CategoryService::class.java, context)

    suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        api.getCategories()
    }
}
