package com.example.firstaidfront.repository

import android.content.Context
import com.example.firstaidfront.api.AuthService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.models.AuthResponse

class AuthRepository(context: Context) {
    private val authService = ApiClient.create(AuthService::class.java, context)

    suspend fun exchangeToken(code: String): AuthResponse {
        return authService.login(code)
    }
}