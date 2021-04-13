package com.pqaar.app.ui.UnionAdmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pqaar.app.R
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.utils.TimeConversions
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

        model.getLivePropRoutesList().observe(this, Observer {
            textView14.text = it.toString()
        })

        model.getLiveTruckDataList().observe(this, Observer {
            textView15.text = it.toString()
        })

        val demoList = ArrayList<LiveRoutesListItem>()


        button2.setOnClickListener {


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