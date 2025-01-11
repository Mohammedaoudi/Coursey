package com.example.firstaidfront.config

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.firstaidfront.api.ParticipantService
import com.example.firstaidfront.models.AuthResponse
import com.example.firstaidfront.models.Participant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_PARTICIPANT_ID = "participant_id"
    private const val KEY_USER_ID = "user_id"

    // User info keys
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return !prefs.getString(KEY_ACCESS_TOKEN, null).isNullOrEmpty()
    }

    fun saveAuthData(context: Context, authResponse: AuthResponse) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_ACCESS_TOKEN, authResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
            putInt(KEY_PARTICIPANT_ID, authResponse.participantId)
            putString(KEY_USER_ID, authResponse.userId)
        }
        logAllData(context) // Log all SharedPreferences data
    }

    suspend fun fetchAndSaveParticipantInfo(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val userId = getUserId(context) ?: return@withContext
                val participantService = ApiClient.create(ParticipantService::class.java, context)
                val participant = participantService.getParticipant(userId)

                saveParticipantInfo(context, participant)
            } catch (e: Exception) {
                Log.e("TokenManager", "Error fetching participant info", e)
            }
        }
    }

    private fun saveParticipantInfo(context: Context, participant: Participant) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_FIRST_NAME, participant.firstName)
            putString(KEY_LAST_NAME, participant.lastName)
        }
        logAllData(context) // Log all SharedPreferences data
    }

    fun getUserId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_USER_ID, null)
    }

    fun getFullName(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val firstName = prefs.getString(KEY_FIRST_NAME, "") ?: ""
        val lastName = prefs.getString(KEY_LAST_NAME, "") ?: ""
        return "$firstName $lastName"
    }

    fun getAccessToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_REFRESH_TOKEN, null)
    }

    fun getParticipantId(context: Context): Int? {
        val id = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_PARTICIPANT_ID, -1)
        return if (id != -1) id else null
    }

    fun clearAuthData(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        logAllData(context) // Log all SharedPreferences data after clearing
    }

    /**
     * Logs all data stored in SharedPreferences for debugging purposes.
     */
    private fun logAllData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val allEntries = prefs.all
        if (allEntries.isEmpty()) {
            Log.d("TokenManager", "SharedPreferences is empty.")
        } else {
            Log.d("TokenManager", "Logging all data in SharedPreferences:")
            for ((key, value) in allEntries) {
                Log.d("TokenManager", "Key: $key, Value: $value")
            }
        }
    }
}
