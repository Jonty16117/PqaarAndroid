package com.pqaar.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.pqaar.app.R

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {

    @Suppress("PrivatePropertyName")
    private val SPLASH_SCREEN_TIMEOUT = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, SPLASH_SCREEN_TIMEOUT.toLong())
    }
}