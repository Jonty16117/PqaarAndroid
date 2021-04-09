package com.pqaar.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pqaar.app.R

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activity)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, LoginUserFragment()).commit()
    }
}