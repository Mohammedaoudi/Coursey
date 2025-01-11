package com.example.firstaidfront.data

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.firstaidfront.models.AuthResponse
import com.example.firstaidfront.repository.AuthRepository

class AuthViewModel(context: Context) : ViewModel() {
    private val repository = AuthRepository(context)

    suspend fun handleAuthCode(code: String): AuthResponse {
        return repository.exchangeToken(code)
    }
}