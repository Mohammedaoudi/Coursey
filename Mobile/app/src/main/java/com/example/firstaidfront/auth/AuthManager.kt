package com.example.firstaidfront.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import net.openid.appauth.*
import com.example.firstaidfront.config.AuthConfig

class AuthManager(private val context: Context) {
    private val TAG = "AuthManager"

    val authService = AuthorizationService(context)



    fun getAuthUrl(): String {
        val baseUrl = AuthConfig.AUTH_URI
        val redirectUri = AuthConfig.REDIRECT_URI
        val clientId = AuthConfig.CLIENT_ID
        val responseType = "code"
        val scope = AuthConfig.SCOPE
        val state = "1234"

        val authUrl = "$baseUrl?" +
                "client_id=$clientId&" +
                "redirect_uri=$redirectUri&" +
                "response_type=$responseType&" +
                "scope=${Uri.encode(scope)}&" +
                "state=$state"

        Log.d(TAG, "Generated auth URL: $authUrl with redirect URI: $redirectUri")
        return authUrl
    }



    fun extractAuthCode(intent: android.content.Intent): String? {
        val uri = intent.data
        Log.d(TAG, "Extracting auth code from intent data: $uri")

        if (uri == null) {
            Log.e(TAG, "URI is null")
            throw IllegalStateException("No URI in intent")
        }

        // Extract the code directly from the URI
        val code = uri.getQueryParameter("code")
        if (code == null) {
            Log.e(TAG, "No code parameter in URI")
            throw IllegalStateException("No code parameter in redirect URI")
        }

        Log.d(TAG, "Successfully extracted code: ${code.take(5)}...")
        return code
    }

    fun dispose() {
        authService.dispose()
    }
}