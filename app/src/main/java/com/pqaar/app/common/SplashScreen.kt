package com.pqaar.app.common

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.pahunchAdmin.view.PahunchAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard
import com.pqaar.app.utils.DbPaths
import com.pqaar.app.utils.DbPaths.USER_TYPE

@Suppress("DEPRECATION")
class SplashScreen : AppCompatActivity() {
    private val TAG = "SplashScreen"
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    @Suppress("PrivatePropertyName")
    private val SPLASH_SCREEN_TIMEOUT = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        checkLoggedInStatus()

        /*Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, SPLASH_SCREEN_TIMEOUT.toLong())*/
    }

    private fun checkLoggedInStatus() {
        val user = auth.currentUser
        if (user != null) {
            db.collection(DbPaths.USER_DATA).document(user.uid).get(Source.SERVER)
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        when (document.get(USER_TYPE)) {
                            "MA" -> {
                                val intent = Intent(this, MandiAdminDashboard::class.java)
                                try {
                                    startActivity(intent)
                                } finally {
                                    finish()
                                }
                            }
                            "TO" -> {
                                val intent = Intent(this, TruckOwnerDashboard::class.java)
                                try {
                                    startActivity(intent)
                                } finally {
                                    finish()
                                }
                            }
                            "PA" -> {
                                val intent = Intent(this, PahunchAdminDashboard::class.java)
                                try {
                                    startActivity(intent)
                                } finally {
                                    finish()
                                }
                            }
                        }
                    }
                }
        }
        else {
            Handler().postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, SPLASH_SCREEN_TIMEOUT.toLong())
        }
    }
}