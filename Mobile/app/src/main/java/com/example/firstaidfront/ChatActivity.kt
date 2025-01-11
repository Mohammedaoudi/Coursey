package com.example.firstaidfront

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.firstaidfront.adapter.ChatAdapter
import com.example.firstaidfront.databinding.ActivityChatBinding
import com.example.firstaidfront.models.Message
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREF_NAME = "chat_prefs"
        private const val MESSAGES_KEY = "messages"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        loadMessages()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Add clear chat option in menu
        binding.toolbar.inflateMenu(R.menu.chat_menu)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_clear -> {
                    clearChat()
                    true
                }
                else -> false
            }
        }

        binding.clearButton.setOnClickListener {
            showClearDialog()
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.messagesRecyclerView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    private fun setupMessageInput() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun showClearDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Chat")
            .setMessage("Are you sure you want to clear all messages?")
            .setPositiveButton("Clear") { _, _ ->
                chatAdapter.clearMessages()
                sharedPreferences.edit().remove(MESSAGES_KEY).apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun sendMessage(text: String) {
        Log.d("ChatDebug", "Sending message: $text")

        val userMessage = Message(text, true)
        chatAdapter.addMessage(userMessage)
        saveMessages()
        scrollToBottom()

        binding.loadingAnimation.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val jsonBody = JSONObject().put("question", text)
                Log.d("ChatDebug", "Request JSON: ${jsonBody}")

                val response = withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // Connection timeout
                        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // Read timeout
                        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)  // Write timeout
                        .build()

                    val request = Request.Builder()
                        .url("https://fce0-34-125-7-43.ngrok-free.app/ask")
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create("application/json".toMediaType(), jsonBody.toString()))
                        .build()

                    Log.d("ChatDebug", "Request URL: ${request.url}")
                    Log.d("ChatDebug", "Request Headers: ${request.headers}")

                    client.newCall(request).execute().use { response ->
                        // Use .use to automatically close the response
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            Log.d("ChatDebug", "Response successful: $responseBody")
                            responseBody
                        } else {
                            Log.e("ChatDebug", "Response error code: ${response.code}")
                            null
                        }
                    }
                }

                binding.loadingAnimation.visibility = View.GONE

                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    val answer = jsonResponse.getString("answer")
                    Log.d("ChatDebug", "Parsed answer: $answer")

                    val botMessage = Message(answer, false)
                    chatAdapter.addMessage(botMessage)
                    saveMessages()
                    scrollToBottom()
                } else {
                    showError("Error receiving response")
                }
            } catch (e: Exception) {
                Log.e("ChatDebug", "Exception during API call", e)
                showError("Error: ${e.message}")
            }
        }
    }
    private fun scrollToBottom() {
        binding.messagesRecyclerView.post {
            binding.messagesRecyclerView.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.error_color, null))
            .setTextColor(resources.getColor(R.color.white, null))
            .show()
    }

    private fun saveMessages() {
        val messagesJson = Gson().toJson(chatAdapter.getMessages())
        sharedPreferences.edit().putString(MESSAGES_KEY, messagesJson).apply()
    }

    private fun loadMessages() {
        val messagesJson = sharedPreferences.getString(MESSAGES_KEY, null)
        if (!messagesJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<Message>>() {}.type
            val messages = Gson().fromJson<List<Message>>(messagesJson, type)
            chatAdapter.setMessages(messages)
            scrollToBottom()
        }
    }

    private fun clearChat() {
        AlertDialog.Builder(this)
            .setTitle("Clear Chat")
            .setMessage("Are you sure you want to clear all messages?")
            .setPositiveButton("Clear") { _, _ ->
                chatAdapter.clearMessages()
                sharedPreferences.edit().remove(MESSAGES_KEY).apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

