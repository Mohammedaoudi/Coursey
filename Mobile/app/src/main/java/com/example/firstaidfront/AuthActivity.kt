package com.example.firstaidfront


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.firstaidfront.auth.AuthManager
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.data.AuthViewModel
import kotlinx.coroutines.launch
import android.webkit.CookieManager
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AuthActivity : AppCompatActivity() {
    private val TAG = "AuthActivity"

    private lateinit var loadingMessages: Array<String>
    private var currentMessageIndex = 0
    private val messageHandler = Handler(Looper.getMainLooper())

    private lateinit var authManager: AuthManager
    private val viewModel=AuthViewModel(this)
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        loadingMessages = arrayOf(
            "Authenticating...",
            "Establishing secure connection...",
            "Almost there...",
            "Setting up your profile..."
        )

        progressBar = findViewById(R.id.progressBar)
        authManager = AuthManager(this)

        // Enhanced logging for onCreate
        Log.d(TAG, "onCreate called")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent data: ${intent?.data}")
        Log.d(TAG, "Intent scheme: ${intent?.data?.scheme}")
        Log.d(TAG, "Intent path: ${intent?.data?.path}")
        Log.d(TAG, "SavedInstanceState: $savedInstanceState")

        when {
            intent?.action == Intent.ACTION_VIEW && intent.data != null -> {
                Log.d(TAG, "Handling redirect from auth server")
                handleAuthResult(intent)
            }
            savedInstanceState == null -> {
                Log.d(TAG, "Starting new auth flow")
                startAuth()
            }
        }
    }

    private fun startLoadingAnimation() {
        messageHandler.postDelayed(object : Runnable {
            override fun run() {
                findViewById<TextView>(R.id.loadingText)?.let { textView ->
                    textView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            currentMessageIndex = (currentMessageIndex + 1) % loadingMessages.size
                            textView.text = loadingMessages[currentMessageIndex]
                            textView.animate()
                                .alpha(1f)
                                .setDuration(500)
                                .start()
                        }
                        .start()
                }
                messageHandler.postDelayed(this, 3000)
            }
        }, 3000)
    }

    private fun startAuth() {
        try {
            val authUrl = authManager.getAuthUrl()
            Log.d(TAG, "Starting auth with URL: $authUrl")

            // Enhanced CustomTabs styling
            val customTabsIntent = CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(this, R.color.pink_dark)) // Use pink color
                .setShowTitle(true)
                .setNavigationBarColor(ContextCompat.getColor(this, R.color.pink_light))
                .setNavigationBarDividerColor(ContextCompat.getColor(this, R.color.pink_light))
                .setStartAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .setUrlBarHidingEnabled(true)
                .build()

            // Update intent flags
            customTabsIntent.intent.apply {
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                // Set app name in recent tasks
                putExtra(Intent.EXTRA_TITLE, "Coursey Login")
            }

            customTabsIntent.launchUrl(this, Uri.parse(authUrl))
            Log.d(TAG, "Successfully launched auth URL")
        } catch (e: Exception) {
            Log.e(TAG, "Auth start failed", e)
            showError("Auth start failed: ${e.message}")
        }
    }

    private fun handleAuthResult(intent: Intent) {
        val uri = intent.data
        Log.d(TAG, "Handling redirect URI: $uri")
        Log.d(TAG, "URI scheme: ${uri?.scheme}")
        Log.d(TAG, "URI host: ${uri?.host}")
        Log.d(TAG, "URI path: ${uri?.path}")
        Log.d(TAG, "URI query: ${uri?.query}")

        if (uri?.scheme == "com.firstaid.app") {
            Log.d(TAG, "Valid scheme detected")
            showLoading()

            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Beginning auth code extraction")
                    val authCode = authManager.extractAuthCode(intent)
                    Log.d(TAG, "Extracted auth code: ${authCode?.take(5)}...")

                    if (authCode != null) {
                        Log.d(TAG, "Starting token exchange")
                        val authResponse = viewModel.handleAuthCode(authCode)
                        Log.d(TAG, "Token exchange successful: ${authResponse.accessToken.take(10)}...")

                        Log.d(TAG, "Saving auth data")
                        TokenManager.saveAuthData(this@AuthActivity, authResponse)

                        Log.d(TAG, "Clearing browser data")
                        clearBrowserData()

                        Log.d(TAG, "Starting main activity")
                        startMainActivity()
                    } else {
                        Log.e(TAG, "Auth code is null")
                        throw IllegalStateException("Auth code is null")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Auth failed", e)
                    hideLoading()
                    showError("Authentication failed: ${e.message}")
                }
            }
        } else {
            Log.e(TAG, "Invalid redirect URI - scheme: ${uri?.scheme}")
            showError("Invalid redirect URI")
        }
    }

    private fun clearBrowserData() {
        try {
            Log.d(TAG, "Clearing browser cookies")
            CookieManager.getInstance().apply {
                removeAllCookies(null)
                flush()
            }

            Log.d(TAG, "Clearing WebView data")
            android.webkit.WebStorage.getInstance().deleteAllData()
            Log.d(TAG, "Successfully cleared browser data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear browser data", e)
        }
    }

    private fun startMainActivity() {
        showSuccessAlert("Login Successful!", "Welcome to Coursey App") {
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }
    }

    private fun showSuccessAlert(title: String, message: String, onDismiss: () -> Unit) {
        // Hide loading animation and progress bar first
        findViewById<LottieAnimationView>(R.id.loadingAnimation)?.visibility = View.GONE
        findViewById<TextView>(R.id.loadingText)?.visibility = View.GONE
        findViewById<TextView>(R.id.loadingSubtext)?.visibility = View.GONE
        findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressBar)?.visibility = View.GONE

        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)

        val dialog = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Setup dialog views with enhanced styling
        dialogView.apply {
            findViewById<LottieAnimationView>(R.id.successAnimation).apply {
                playAnimation()
                setAnimation(R.raw.success_animation)
            }

            findViewById<TextView>(R.id.dialogTitle).apply {
                text = title
                setTextColor(ContextCompat.getColor(context, R.color.pink_dark))
                typeface = ResourcesCompat.getFont(context, R.font.poppins_bold)
            }

            findViewById<TextView>(R.id.dialogMessage).apply {
                text = message
                setTextColor(ContextCompat.getColor(context, R.color.black))
                typeface = ResourcesCompat.getFont(context, R.font.poppins_regular)
            }

            findViewById<MaterialButton>(R.id.btnContinue).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.pink_dark))
                setOnClickListener {
                    dialog.dismiss()
                    onDismiss()
                }
            }
        }

        dialog.window?.apply {
            setBackgroundDrawableResource(R.drawable.dialog_background)
            // Add animation for dialog
            attributes?.windowAnimations = R.style.DialogAnimation
        }

        dialog.show()
    }

    private fun showError(message: String) {
        Log.e(TAG, "Showing error: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        Log.d(TAG, "Showing loading indicator")
        progressBar.visibility = View.VISIBLE
        // Add this line to start the loading animation
        startLoadingAnimation()
    }

    private fun hideLoading() {
        Log.d(TAG, "Hiding loading indicator")
        progressBar.visibility = View.GONE
        // Add this line to stop the loading animation
        messageHandler.removeCallbacksAndMessages(null)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent data: ${intent?.data}")
        Log.d(TAG, "Intent scheme: ${intent?.data?.scheme}")
        Log.d(TAG, "Intent path: ${intent?.data?.path}")
        Log.d(TAG, "Intent query: ${intent?.data?.query}")

        setIntent(intent)

        intent?.let {
            if (it.action == Intent.ACTION_VIEW) {
                Log.d(TAG, "Processing VIEW action in onNewIntent")
                handleAuthResult(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Add this line to clean up the message handler
        messageHandler.removeCallbacksAndMessages(null)
        authManager.dispose()
    }
}