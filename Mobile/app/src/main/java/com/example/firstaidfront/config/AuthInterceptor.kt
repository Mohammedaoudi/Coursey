package com.example.firstaidfront.config

import android.content.Context
import android.util.Log
import com.example.firstaidfront.api.AuthService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    private val publicPaths = listOf(
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/register",
        "/api/public"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Logging the original request URL
        Log.d("AuthInterceptor", "Intercepting request to URL: ${originalRequest.url}")

        // Skip authentication for public paths
        if (publicPaths.any { path -> originalRequest.url.encodedPath.contains(path) }) {
            Log.d("AuthInterceptor", "Request is public. No authentication required.")
            return chain.proceed(originalRequest)
        }

        // Add token to request
        val request = originalRequest.newBuilder().apply {
            TokenManager.getAccessToken(context)?.let {
                Log.d("AuthInterceptor", "Adding Authorization header with access token.")
                header("Authorization", "Bearer $it")
            } ?: Log.d("AuthInterceptor", "No access token available.")
        }.build()

        var response = chain.proceed(request)
        Log.d("AuthInterceptor", "Response received. Status code: ${response.code}")

        // If we get a 403, try to refresh the token
        if (response.code == 401) {
            Log.d("AuthInterceptor", "Received 403. Attempting to refresh token.")
            response.close()

            TokenManager.getRefreshToken(context)?.let { refreshToken ->
                try {
                    val authService = ApiClient.create(AuthService::class.java, context)
                    val authResponse = runBlocking {
                        Log.d("AuthInterceptor", "Refreshing token using refreshToken: $refreshToken")
                        authService.refreshToken(refreshToken)
                    }

                    Log.d("AuthInterceptor", "Token refreshed successfully. Saving new tokens.")
                    TokenManager.saveAuthData(context, authResponse)

                    // Retry the original request with new token
                    Log.d("AuthInterceptor", "Retrying original request with new access token.")
                    return chain.proceed(
                        originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${authResponse.accessToken}")
                            .build()
                    )
                } catch (e: Exception) {
                    Log.e("AuthInterceptor", "Failed to refresh token: ${e.message}", e)
                    TokenManager.clearAuthData(context)
                }
            } ?: Log.d("AuthInterceptor", "No refresh token available.")
        }

        return response
    }
}
