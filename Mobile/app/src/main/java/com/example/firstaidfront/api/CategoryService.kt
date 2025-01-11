package com.example.firstaidfront.api

import com.example.firstaidfront.models.Category
import retrofit2.http.GET
import retrofit2.http.Query

interface CategoryService {
    @GET("TRAINING-SERVICE/api/categories")
    suspend fun getCategories(): List<Category>

    @GET("TRAINING-SERVICE/api/categories/search")
    suspend fun searchCategories(@Query("query") searchQuery: String): List<Category>
}