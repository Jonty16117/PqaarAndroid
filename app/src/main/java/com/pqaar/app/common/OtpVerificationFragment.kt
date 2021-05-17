package com.pqaar.app.common

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.pahunchAdmin.view.PahunchAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.USER_TYPE
import java.util.concurrent.TimeUnit


class OtpVerificationFragment : Fragment() {
    private val TAG = "OtpVerificationFragment"
    private val db = Firebase.firestore

    private lateinit var btnVerifyOtp: Button
    private lateinit var textInputOtp: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: Button


    var mAuth: FirebaseAuth? = null
    var phoneNumber: String = ""
    var verificationID: String = ""
    var token_: String = ""
    var userType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_otp_verification, container, false)

        btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp)
        textInputOtp = view.findViewById(R.id.textInputOtp)
        progressBar = view.findViewById(R.id.progressBar4)
        closeButton = view.findViewById(R.id.button)

        phoneNumber = "+91${arguments?.getString("inputTextPhoneNo")!!}"
        userType = arguments?.getString("userType")!!
        mAuth = FirebaseAuth.getInstance()
        closeButton.setOnClickListener {
            val alertDialog: AlertDialog
            val builder = AlertDialog.Builder(activity!!, R.style.CustomAlertDialog)
            builder.setTitle("Exit")
            builder.setMessage("Are you sure you want to exit?")
            builder.setIcon(R.drawable.ic_close_dark)
            builder.setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                activity!!.finish()
                System.exit(0)
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
            logoutBtn.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark))
            cancelBtn.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark))
            messageText?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimaryDark))

        }

        progressBar.isVisible = true
        sendVerificationCode(phoneNumber)

        btnVerifyOtp.setOnClickListener {
            if (textInputOtp.text.toString().isEmpty() || verificationID.isEmpty()) {
                Toast.makeText(context, "Enter valid OTP!", Toast.LENGTH_LONG).show()
            } else {
                val credential =
                    PhoneAuthProvider.getCredential(verificationID, (textInputOtp.text.toString()))
                signInWithPhoneAuthCredential(credential, true)
            }
        }
        return view
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNumber)
            .setActivity(requireActivity())
            .setTimeout(60, TimeUnit.SECONDS)
            .setCallbacks(mCallBacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    val mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
            progressBar.isVisible = false
            if (credential != null) {
                signInWithPhoneAuthCredential(credential, false)

            }
        }

        override fun onVerificationFailed(p0: FirebaseException?) {
            progressBar.isVisible = false
            Toast.makeText(context, "OTP Verification Failed!", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String?,
            token: PhoneAuthProvider.ForceResendingToken?
        ) {
            super.onCodeSent(verificationId, token)
            progressBar.isVisible = false
            verificationID = verificationId.toString()
            token_ = token.toString()
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String?) {
            super.onCodeAutoRetrievalTimeOut(verificationId!!)
            progressBar.isVisible = false
        }
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        simInOtherPhone: Boolean
    ) {
        progressBar.isVisible = true
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                activity!!
            ) { task ->
                progressBar.isVisible = false
                if (task.isSuccessful) {
                    val user = task.result?.user
                    progressBar.isVisible = true
                    db.collection(USER_DATA).document(user!!.uid).get(Source.SERVER)
                        .addOnSuccessListener { document ->
                            progressBar.isVisible = false
                            if (document.exists()) {
                                when (userType) {
                                    "mandi-admin" -> {
                                        if (document.get(USER_TYPE) == "MA") {
                                            val intent = Intent(activity, MandiAdminDashboard::class.java)
                                            try {
                                                startActivity(intent)
                                            } finally {
                                                activity!!.finish()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Invalid user, please select correct user type!!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                    "truck-owner" -> {
                                        if (document.get(USER_TYPE) == "TO") {
                                            val intent = Intent(activity, TruckOwnerDashboard::class.java)
                                            try {
                                                startActivity(intent)
                                            } finally {
                                                activity!!.finish()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Invalid user, please select correct user type!!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                    "pahunch-admin" -> {
                                        if (document.get(USER_TYPE) == "PA") {
                                            Log.d(TAG, "In pa switch block")
                                            val intent = Intent(activity, PahunchAdminDashboard::class.java)
                                            try {
                                                startActivity(intent)
                                            } finally {
                                                activity!!.finish()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Invalid user, please select correct user type!!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    context,
                                    "User not registered, please register first!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    if (!simInOtherPhone) {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(context, "Invalid OTP!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }
}