package com.pqaar.app.common

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard
import java.util.concurrent.TimeUnit


class OtpVerificationFragment : Fragment() {

    private lateinit var btnVerifyOtp: Button
    private lateinit var textInputOtp: EditText

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

        phoneNumber = "+91${arguments?.getString("inputTextPhoneNo")!!}"
        userType = arguments?.getString("userType")!!

        mAuth = FirebaseAuth.getInstance()

        btnVerifyOtp.setOnClickListener {
            loginTask()
        }

        return view
    }


    private fun loginTask() {

        val mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                if (credential != null) {
                    signInWithPhoneAuthCredential(credential)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Toast.makeText(context, "OTP Verification Failed!", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String?,
                token: PhoneAuthProvider.ForceResendingToken?
            ) {
                super.onCodeSent(verificationId, token)
                verificationID = verificationId.toString()
                token_ = token.toString()

                btnVerifyOtp.setOnClickListener {
                    verifyAuthentication(verificationID, textInputOtp.text.toString())
                }

                Log.e("Login : verificationId ", verificationId!!)
                Log.e("Login : token ", token_)

                startActivity(Intent(
                    context,
                    MandiAdminDashboard::class.java
                ).apply { putExtra("null", "null") })
                /*startActivity(Intent(
                        context,
                        TruckOwnerDashboardActivity::class.java)
                        .apply {putExtra("null", "null")})*/

            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String?) {
                super.onCodeAutoRetrievalTimeOut(verificationId!!)
            }
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            activity!!,
            mCallBacks
        );

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                activity!!
            ) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    startActivity(Intent(activity, TruckOwnerDashboard::class.java))

                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(context, "Invalid OTP!", Toast.LENGTH_LONG).show()
                    }
                }
            }
    }

    private fun verifyAuthentication(verificationID: String, otpText: String) {

        val phoneAuthCredential =
            PhoneAuthProvider.getCredential(verificationID, otpText) as PhoneAuthCredential
        signInWithPhoneAuthCredential(phoneAuthCredential)
    }
}