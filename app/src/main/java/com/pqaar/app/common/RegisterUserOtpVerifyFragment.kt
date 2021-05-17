package com.pqaar.app.common

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.pahunchAdmin.view.PahunchAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard
import com.pqaar.app.utils.DbPaths
import com.pqaar.app.utils.DbPaths.EMAIL
import com.pqaar.app.utils.DbPaths.FIRST_NAME
import com.pqaar.app.utils.DbPaths.LAST_NAME
import com.pqaar.app.utils.DbPaths.MANDI
import com.pqaar.app.utils.DbPaths.PHONE_NO
import com.pqaar.app.utils.DbPaths.TRUCKS
import com.pqaar.app.utils.DbPaths.USER_DATA
import com.pqaar.app.utils.DbPaths.USER_TYPE
import java.util.concurrent.TimeUnit

class RegisterUserOtpVerifyFragment : Fragment() {
    private val TAG = "RegisterUserOtpVerifyFragment"

    private lateinit var btnVerifyOtp: Button
    private lateinit var textInputOtp: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var closeButton: Button

    var mAuth: FirebaseAuth? = null
    val db = Firebase.firestore
    var phoneNumber: String = ""
    var verificationID: String = ""
    var token_: String = ""
    var userType: String = ""
    var firstName: String = ""
    var lastName: String = ""
    var email: String = ""

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
        firstName = arguments?.getString("firstName")!!
        lastName = arguments?.getString("lastName")!!
        email = arguments?.getString("email")!!
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
                signInWithPhoneAuthCredential(credential, false)
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
            if (credential != null) {
                progressBar.isVisible = false
                signInWithPhoneAuthCredential(credential, true)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException?) {
            progressBar.isVisible = false
            Log.d(TAG, p0.toString())
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
        progressBar.isVisible = false
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                activity!!
            ) { task ->
                progressBar.isVisible = false
                if (task.isSuccessful) {
                    progressBar.isVisible = true
                    val user = task.result?.user!!
                    db.collection(USER_DATA).document(user.uid).get(Source.SERVER)
                        .addOnSuccessListener { document ->
                            progressBar.isVisible = false
                            if (document.exists()) {
                                Toast.makeText(
                                    context,
                                    "User already registered!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                addNewUser(userType, user.uid)
                            }
                        }
                        .addOnFailureListener {
                            progressBar.isVisible = false
                            Toast.makeText(
                                context,
                                "Registration failed, please try again after 5 minutes!",
                                Toast.LENGTH_LONG
                            ).show()
                        }


                } else {
                    if (!simInOtherPhone) {
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Log.d(TAG, task.exception.toString())
                            Toast.makeText(context, "Invalid OTP!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
    }

    fun addNewUser(userType: String, userUId: String) {
        val data = HashMap<String, Any?>()
        data[EMAIL] = email
        data[PHONE_NO] = phoneNumber
        data[FIRST_NAME] = firstName
        data[LAST_NAME] = lastName
        when (userType) {
            "mandi-admin" -> {
                data[USER_TYPE] = "MA"
//                data[MANDI] = "MA"
                db.collection(USER_DATA).document(userUId)
                    .set(data).addOnSuccessListener {
                        //enter user's mandi in db
                        Toast.makeText(
                            context,
                            "New Mandi Admin Created!",
                            Toast.LENGTH_LONG
                        ).show()
//                        startActivity(Intent(activity, MandiAdminDashboard::class.java))
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to create new mandi admin user, please try again after 5 minutes!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }

            "pahunch-admin" -> {
                data[USER_TYPE] = "PA"
                db.collection(USER_DATA).document(userUId)
                    .set(data).addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "New Pahunch Admin Created!",
                            Toast.LENGTH_LONG
                        ).show()
//                        startActivity(
//                            Intent(
//                                activity,
//                                PahunchAdminDashboard::class.java
//                            )
//                        )
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to create new pahunch admin user, please try again after 5 minutes!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }

            "truck-owner" -> {
                data[USER_TYPE] = "TO"
                data[TRUCKS] = null
                db.collection(USER_DATA).document(userUId)
                    .set(data).addOnSuccessListener {
                        Toast.makeText(
                            context,
                            "New Truck Owner Created!",
                            Toast.LENGTH_LONG
                        ).show()
//                        startActivity(Intent(activity, TruckOwnerDashboard::class.java))
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to create new truck owner user, please try again after 5 minutes!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

    }

}