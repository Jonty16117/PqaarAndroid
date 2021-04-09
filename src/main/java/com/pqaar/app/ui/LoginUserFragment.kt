package com.pqaar.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.coroutines.*
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.pqaar.app.R
import com.pqaar.app.model.HistoryAuctionListItemDTO
import com.pqaar.app.repository.UnionAdminRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancelAndJoin
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


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
    private lateinit var radioBtnUnionAdmin: RadioButton
    private lateinit var radioBtnTruckOwner: RadioButton

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
        radioBtnUnionAdmin = view.findViewById(R.id.radioBtnUnionAdmin)
        radioBtnTruckOwner = view.findViewById(R.id.radioBtnTruckOwner)
        var checkedPhoneNumber: String?

        GlobalScope.launch(Dispatchers.IO) {
            var lastAuctionListDocument = ""
            val executionTime = measureTimeMillis {
                lastAuctionListDocument = UnionAdminRepository.fetchLastAuctionListDocument()
                UnionAdminRepository.getLastAuctionList(lastAuctionListDocument)
                if(UnionAdminRepository.getLiveTruckDataList()) {
                    UnionAdminRepository.separateOpenCloseLists()
                    UnionAdminRepository.getLastMissedList()
                    UnionAdminRepository.combineLists()
                }
            }
            /**
             * [LiveAuctionListItem(currentNo=, prevNo=0, truckNo=pb0, locked=true, src=, des=, timestamp=),
             * LiveAuctionListItem(currentNo=, prevNo=1, truckNo=pb1, locked=true, src=, des=, timestamp=),
             * LiveAuctionListItem(currentNo=, prevNo=2, truckNo=pb2, locked=true, src=, des=, timestamp=),
             * LiveAuctionListItem(currentNo=, prevNo=3, truckNo=PB30XXXX, locked=true, src=, des=, timestamp=)]
             */
            withContext(Dispatchers.Main){
                Log.d(TAG, "ExecutionTime = $executionTime")
                /*Toast.makeText(context,
                    "lastAuctionListDocument = $lastAuctionListDocument",
                    Toast.LENGTH_LONG).show()*/
                Log.d(TAG, "liveCombinedAuctionList = ${UnionAdminRepository.liveCombinedAuctionList}")
                Toast.makeText(
                    context,
                    "liveCombinedAuctionList = ${UnionAdminRepository.liveCombinedAuctionList}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnLoginWithPhone.setOnClickListener {
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

        btnlogin.setOnClickListener {
            startActivity(Intent(
                context,
                TruckOwnerDashboardActivity::class.java
            ).apply { putExtra("null", "null") })
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

    private fun getDataFromText(): String? {
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