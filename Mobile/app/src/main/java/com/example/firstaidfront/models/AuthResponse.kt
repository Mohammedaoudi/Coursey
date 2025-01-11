package com.example.firstaidfront.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    // Change these annotations to match the exact keys from JSON
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("participantId")
    val participantId: Int,
    @SerializedName("userId")
    val userId: String
)