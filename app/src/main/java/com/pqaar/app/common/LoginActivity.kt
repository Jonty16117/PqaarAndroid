package com.pqaar.app.common

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.pahunchAdmin.view.PahunchAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard
import com.pqaar.app.utils.DbPaths

class LoginActivity: AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activity)



        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment, LoginUserFragment()).commit()
    }



    override fun onBackPressed() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?")
        builder.setIcon(R.drawable.ic_close_dark)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            super.onBackPressed()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        alertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()

        val messageText = alertDialog.findViewById<TextView>(android.R.id.message)
        val logoutBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val cancelBtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        logoutBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        cancelBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        messageText?.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))

    }

}