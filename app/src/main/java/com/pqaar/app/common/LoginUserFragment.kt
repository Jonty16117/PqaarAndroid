package com.pqaar.app.common

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.view.MandiAdminDashboard
import com.pqaar.app.pahunchAdmin.view.PahunchAdminDashboard
import com.pqaar.app.truckOwner.view.TruckOwnerDashboard


class LoginUserFragment : Fragment() {
    private val TAG = "LoginUserFragment"

    val auth = FirebaseAuth.getInstance()
    val lastAuctionList = ArrayList<Pair<String, Any>>()

    private lateinit var btnregister: Button
    private lateinit var btnlogin: Button
    private lateinit var textInputPhone: EditText
    private lateinit var btnLoginWithPhone: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioBtnMandiAdmin: RadioButton
    private lateinit var radioBtnTruckOwner: RadioButton
    private lateinit var radioBtnPahunchAdmin: RadioButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login_user, container, false)

        btnregister = view.findViewById(R.id.btnregister)
        btnlogin = view.findViewById(R.id.btnlogin)
        textInputPhone = view.findViewById(R.id.textInputPhone)
        btnLoginWithPhone = view.findViewById(R.id.btnLoginWithPhone)
        radioGroup = view.findViewById(R.id.radioGroup)
        radioBtnMandiAdmin = view.findViewById(R.id.radioBtnMandiAdmin)
        radioBtnTruckOwner = view.findViewById(R.id.radioBtnTruckOwner)
        radioBtnPahunchAdmin = view.findViewById(R.id.radioBtnPahunchAdmin)
        var checkedPhoneNumber: String?


        /**
         * FOR TESTING
         */





        /**
         * NOT FOR TESTING
         */
        btnLoginWithPhone.setOnClickListener {
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(context, "Please Select User Type", Toast.LENGTH_LONG).show()
            } else {
                checkedPhoneNumber = getPhoneNoFromEditText()
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

        btnlogin.setOnClickListener {
            when (getUserType()) {
                "mandi-admin" -> {
                    startActivity(Intent(
                        context,
                        MandiAdminDashboard::class.java
                    ).apply { putExtra("null", "null") })
                }
                "truck-owner" -> {
                    startActivity(Intent(
                        context,
                        TruckOwnerDashboard::class.java
                    ).apply { putExtra("null", "null") })
                }
                "pahunch-admin" -> {
                    startActivity(Intent(
                        context,
                        PahunchAdminDashboard::class.java
                    ).apply { putExtra("null", "null") })
                }
                else -> {
                    Toast.makeText(context, "Please Select User Type", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnregister.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment, RegisterUserFragment())
                ?.addToBackStack(null)
                ?.commit()
        }

        return view
    }

    private fun getPhoneNoFromEditText(): String? {
        return if (textInputPhone.text.trim().isEmpty() ||
            textInputPhone.text.trim().length != 10
        ) {
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
            (radioBtnTruckOwner.id) -> {
                return "truck-owner"
            }
            (radioBtnPahunchAdmin.id) -> {
                return "pahunch-admin"
            }
            else -> {
                return null
            }
        }
    }
}