package com.pqaar.app.ui.UnionAdmin

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pqaar.app.R
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.repositories.UnionAdminRepo
import com.pqaar.app.utils.TimeConversions
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import com.pqaar.app.viewmodels.UnionAdminViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class UnionAdminDashboard : AppCompatActivity() {
    private val TAG = "UnionAdminDashboard"

    private lateinit var textView14: TextView
    private lateinit var textView15: TextView
    private lateinit var textView16: TextView
    private lateinit var textView19: TextView
    private lateinit var textView20: TextView
    private lateinit var button2: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_union_admin_dashboard)

        textView14 = findViewById(R.id.textView14)
        textView15 = findViewById(R.id.textView15)
        textView16 = findViewById(R.id.textView16)
        textView19 = findViewById(R.id.textView19)
        textView20 = findViewById(R.id.textView20)
        button2 = findViewById(R.id.button2)

        val model = ViewModelProviders.of(this)
            .get(UnionAdminViewModel::class.java)

        model.getLivePropRoutesList().observe(this, {
            textView14.text = "Live Proposed Routes List = ${it}"
        })

        model.getLiveTruckDataList().observe(this, {
            textView15.text = "Live Truck Status List = ${it}"
        })

        model.getAuctionStatus().observe(this, {
            textView16.text = "Live Auction Status = ${it}"
        })

        model.getLiveRoutesList().observe(this, {
            textView19.text = "Live Routes List = ${it}"
        })

        model.getAuctionsBonusTimeInfo().observe(this, {
            textView20.text = "Live Bonus Time Status = ${it}"
        })

        model.setAuctionInfo("Live", CurrDateTimeInMillis() + 20000000)



        val demoList = ArrayList<LiveRoutesListItem>()


        button2.setOnClickListener {
            if (model.getLiveTruckDataList().value.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "Please wait while the Live Truck Data is fetched",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                model.initializeAuction(45000)
            }

            /*GlobalScope.launch(Dispatchers.IO) {
                val executionTime = measureTimeMillis {
                    *//*UnionAdminRepository.setAuctionStatus("Live")
                    UnionAdminRepository
                        .setAuctionTimestamp(
                            TimeConversions
                                .TimestampToMillis("01-12-2021 22:02:20")
                        )*//*
                    *//*model.scheduleNewAuctionList(demoList,
                        CurrTimeInMillis() + 10000, 30000)*//*

                }

                withContext(Dispatchers.Main) {
                    val milli = TimeConversions.TimestampToMillis("01-12-2021 22:02:20")
                    Log.d(TAG, "ExecutionTime = $executionTime")
                    Log.d(TAG, "TimestampToMillis = $milli")
                    Log.d(TAG, "MillisToTimestamp = ${TimeConversions.MillisToTimestamp(milli)}")


                    *//*Toast.makeText(
                            context,
                            "liveCombinedAuctionList = ${UnionAdminRepository.liveCombinedAuctionList}",
                            Toast.LENGTH_LONG
                        ).show()*//*
                }
            }*/
        }
    }
}