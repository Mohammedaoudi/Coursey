package com.example.firstaidfront

import android.annotation.SuppressLint


import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.firstaidfront.config.TokenManager
import com.airbnb.lottie.LottieAnimationView
import android.view.animation.Animation
import android.view.animation.AnimationUtils

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var loadingAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        loadingAnimation = findViewById(R.id.loadingAnimation)

        // Start the animation
        loadingAnimation.apply {
            setAnimation(R.raw.loading_animation)
            playAnimation()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            // Add fade out animation before transitioning
            loadingAnimation.pauseAnimation()
            loadingAnimation.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction {
                    if (TokenManager.isLoggedIn(this)) {
                        startMainActivity()
                    } else {
                        startStartActivity()
                    }
                }
        }, 6000)
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun startStartActivity() {
        startActivity(Intent(this, StartActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::loadingAnimation.isInitialized) {
            loadingAnimation.cancelAnimation()
        }
    }
}