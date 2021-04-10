package com.pqaar.app.ui.common

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pqaar.app.R
import com.pqaar.app.ui.TruckOwner.TruckOwnerDashboardActivity


class RegisterUserFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private lateinit var btnRegisterWithEmail: Button
    private lateinit var btnRegisterWithPhone: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var textInputUsername: EditText
    private lateinit var textInputUsername2: EditText
    private lateinit var textInputPhone: EditText
    private lateinit var textInputEmail: EditText
    private lateinit var textInputPassword: EditText
    private lateinit var textInputPassword2: EditText
    private lateinit var warning: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var radioBtnMandiAdmin: RadioButton
    private lateinit var radioBtnUnionAdmin: RadioButton
    private lateinit var radioBtnTruckOwner: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register_user, container, false)

        btnRegisterWithEmail = view.findViewById(R.id.btnRegisterWithEmail)
        btnRegisterWithPhone = view.findViewById(R.id.btnRegisterWithPhone)
        radioGroup = view.findViewById(R.id.radioGroup)
        textInputUsername = view.findViewById(R.id.textInputUsername)
        textInputUsername2 = view.findViewById(R.id.textInputUsername2)
        textInputPhone = view.findViewById(R.id.textInputPhone)
        textInputEmail = view.findViewById(R.id.textInputEmail)
        textInputPassword = view.findViewById(R.id.textInputPassword)
        textInputPassword2 = view.findViewById(R.id.textInputPassword2)
        warning = view.findViewById(R.id.warning)
        progressBar = view.findViewById(R.id.progressBar)
        radioBtnMandiAdmin = view.findViewById<RadioButton>(R.id.mandiAdmin)
        radioBtnUnionAdmin = view.findViewById<RadioButton>(R.id.unionAdmin)
        radioBtnTruckOwner = view.findViewById<RadioButton>(R.id.truckOwner)
        var checkedPhoneNumber: String?
        var userType: String?

/*        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment, AddTrucksFragment())
            ?.addToBackStack(null)
            ?.commit()*/

        btnRegisterWithPhone.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please Select User Type", Toast.LENGTH_LONG).show()
            } else {
                checkedPhoneNumber = getDataFromText()
                if (checkedPhoneNumber != null) {
                    val dataBundle = Bundle()
                    dataBundle.putString("inputTextPhoneNo", checkedPhoneNumber)
                    dataBundle.putString("userType", getUserType())
                    val otpVerificationFragment = OtpVerificationFragment()
                    otpVerificationFragment.arguments = dataBundle
                    activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.fragment, otpVerificationFragment)
                        ?.addToBackStack(null)
                        ?.commit()
                }
            }
        }

        btnRegisterWithEmail.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please Select User Type", Toast.LENGTH_LONG).show()
            } else {
                //creates new user and stores user data credentials in the cloud
                registerWithEmail()
                saveEmailUserDataToCloud(view!!)
            }
        }

        return view
    }

    @SuppressLint("SetTextI18n")
    private fun registerWithEmail() {

        if (textInputUsername.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please fill your first name!"
        } else if (textInputUsername2.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please fill your last name!"
        } else if (textInputEmail.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please enter your email!"
        } else if (textInputPassword.text.trim().length < 8 || textInputPassword.text.trim().length > 16) {
            warning.isVisible = true
            warning.text = "*Password length can only be between 8 and 16 characters!"
        } else if (textInputPassword.text.trim() != textInputPassword2.text.trim()) {
            warning.isVisible = true
            warning.text = "*Passwords did'nt match, please check your passwords!"
        } else {
            //all fields correct, therefore register user
            warning.isVisible = false
            progressBar.isVisible = true

            auth.createUserWithEmailAndPassword(
                textInputEmail.text.trim().toString(),
                textInputPassword.text.trim().toString()
            ).addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    startActivity(
                        Intent(
                            context,
                            TruckOwnerDashboardActivity::class.java
                        )
                            .apply { putExtra("null", "null") })
                } else {
                    Toast.makeText(
                        context,
                        "Registration Unsuccessful, please try again after 5 minutes!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            progressBar.isVisible = false
        }
    }

    private fun saveEmailUserDataToCloud(view: View) {
        progressBar.isVisible = true

        //add user credentials to cloud firestone
        if (auth.currentUser != null) {
            val data = HashMap<String, Any>()
            data["email"] = textInputEmail.text.trim().toString()
            data["first-name"] = textInputUsername.text.trim().toString()
            data["last-name"] = textInputUsername2.text.trim().toString()
            data["password"] = textInputPassword.text.trim().toString()

            when (getUserType()) {
                "mandi-admin" -> {
                    db.collection("mandi-admin-credentials").document(auth.currentUser.uid).set(data)
                    Toast.makeText(
                        context,
                        "New Mandi Admin Created!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                "union-admin" -> {
                    db.collection("union-admin-credentials").document(auth.currentUser.uid).set(data)
                    Toast.makeText(
                        context,
                        "New Union Admin Created!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                "truck-owner" -> {
                    db.collection("truck-owner-credentials").document(auth.currentUser.uid).set(data)
                    Toast.makeText(
                        context,
                        "New Truck Owner Created!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        } else {
            Toast.makeText(
                context,
                "User data upload failed, please recreate a new account!",
                Toast.LENGTH_LONG
            ).show()
        }
        progressBar.isVisible = false
    }

    private fun getDataFromText(): String? {
        return if (textInputPhone.text.trim().isEmpty() ||
            textInputPhone.text.trim().length != 10) {
            Toast.makeText(
                context,
                "Please enter a valid 10 digits phone number!",
                Toast.LENGTH_LONG
            ).show()
            null
        } else {
            textInputPhone.text.trim().toString()
        }
    }

    private fun getUserType(): String? {
        when (radioGroup.checkedRadioButtonId) {
            (radioBtnMandiAdmin.id) -> {
                return "mandi-admin"
            }

            (radioBtnUnionAdmin.id) -> {
                return "union-admin"
            }

            (radioBtnTruckOwner.id) -> {
                return "truck-owner"
            }
            else -> {
                return null
            }
        }
    }

}

/*    @SuppressLint("SetTextI18n")
    private fun registerWithPhone() {

        var storedVerificationId: String?
        var token: PhoneAuthProvider.ForceResendingToken

        if (textInputUsername.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please fill your first name!"
        } else if (textInputUsername2.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please fill your last name!"
        } else if (textInputEmail.text.trim().isEmpty()) {
            warning.isVisible = true
            warning.text = "*Please enter your email!"
        } else if (textInputPassword.text.trim().length < 8 || textInputPassword.text.trim().length > 16) {
            warning.isVisible = true
            warning.text = "*Password length can only be between 8 and 16 characters!"
        } else if (textInputPassword.text.trim() != textInputPassword2.text.trim()) {
            warning.isVisible = true
            warning.text = "*Passwords did'nt match, please check your passwords!"
        } else {
            //all fields correct, therefore register user
            warning.isVisible = false
            progressBar.isVisible = true

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(("+91" + textInputPhone.text.trim().toString())
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity!!)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)

            //val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)

            var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }

                    // Show a message and update the UI
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                }

            }

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {

                } else {
                }
            }
    }*/

/*
private fun savePhoneUserDataToCloud(view: View) {
    progressBar.isVisible = true

    //add user credentials to cloud firestone
    if (auth.currentUser != null) {
        val data = HashMap<String, Any>()
        data["first-name"] = textInputUsername.text.trim().toString()
        data["last-name"] = textInputUsername2.text.trim().toString()
        data["phone"] = textInputPhone.text.trim().toString()

        when (radioGroup.checkedRadioButtonId) {
            (view.findViewById<RadioButton>(R.id.mandiAdmin).id) -> {
                db.collection("mandi-admin-credentials").document(auth.currentUser.uid).set(data)
                Toast.makeText(
                    context,
                    "New Mandi Admin Created!",
                    Toast.LENGTH_LONG
                ).show()
            }

            (view.findViewById<RadioButton>(R.id.unionAdmin).id) -> {
                db.collection("union-admin-credentials").document(auth.currentUser.uid).set(data)
                Toast.makeText(
                    context,
                    "New Union Admin Created!",
                    Toast.LENGTH_LONG
                ).show()
            }

            (view.findViewById<RadioButton>(R.id.truckOwner).id) -> {
                db.collection("truck-owner-credentials").document(auth.currentUser.uid).set(data)
                Toast.makeText(
                    context,
                    "New Truck Owner Created!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    } else {
        Toast.makeText(
            context,
            "User data upload failed, please recreate a new account!",
            Toast.LENGTH_LONG
        ).show()
    }
    progressBar.isVisible = false
}
*/