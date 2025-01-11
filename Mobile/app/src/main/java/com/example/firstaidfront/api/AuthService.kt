package com.example.firstaidfront.api

import com.example.firstaidfront.models.AuthResponse
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Query("code") code: String): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Query("refreshToken") refreshToken: String): AuthResponse
}