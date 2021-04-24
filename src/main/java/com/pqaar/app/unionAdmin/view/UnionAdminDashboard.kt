package com.pqaar.app.unionAdmin.view

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.pqaar.app.R
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis
import com.pqaar.app.unionAdmin.viewModel.UnionAdminViewModel

class UnionAdminDashboard : AppCompatActivity() {
    private val TAG = "UnionAdminDashboard"

    private lateinit var textView14: TextView
    private lateinit var textView15: TextView
    private lateinit var textView16: TextView
    private lateinit var textView19: TextView
    private lateinit var textView20: TextView
    private lateinit var textView21: TextView
    private lateinit var initAuc: Button
    private lateinit var monitorAuc: Button
    private lateinit var closeAuc: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_union_admin_dashboard)

        textView14 = findViewById(R.id.textView14)
        textView15 = findViewById(R.id.textView15)
        textView16 = findViewById(R.id.textView16)
        textView19 = findViewById(R.id.textView19)
        textView20 = findViewById(R.id.textView20)
        textView21 = findViewById(R.id.textView21)
        initAuc = findViewById(R.id.initAuc)
        monitorAuc = findViewById(R.id.monitorAuc)
        closeAuc = findViewById(R.id.closeAuc)

        val model = ViewModelProviders.of(this)
            .get(UnionAdminViewModel::class.java)

        model.getLivePropRoutesList().observe(this, {
            textView14.text = "Live Proposed Routes List = ${it}"
        })


        model.getLiveTruckDataList().observe(this, {
            textView15.text = "Live Truck Status List = ${it}"
        })

        var liveAuctionStatus = " "
        var liveAuctionStartTime = 0L
        var liveAuctionEndTime = 0L

        model.getAuctionStatus().observe(this, {
            liveAuctionStatus = if (it == null) "NA" else it
            textView16.text = "Live Auction Status: ${liveAuctionStatus}, " +
                    "Start Time: ${liveAuctionStartTime}, End Time: ${liveAuctionEndTime}"
        })

        model.getAuctionStartTime().observe(this, {
            liveAuctionStartTime = if (it == null) 0L else it
            textView16.text = "Live Auction Status: ${liveAuctionStatus}, " +
                    "Start Time: ${liveAuctionStartTime}, End Time: ${liveAuctionEndTime}"
        })

        model.getAuctionEndTime().observe(this, {
            liveAuctionEndTime = if (it == null) 0L else it
            textView16.text = "Live Auction Status: ${liveAuctionStatus}, " +
                    "Start Time: ${liveAuctionStartTime}, End Time: ${liveAuctionEndTime}"
        })

        model.getLiveRoutesList().observe(this, {
            textView19.text = "Live Routes List = ${it}"
        })

        model.getAuctionsBonusTimeInfo().observe(this, {
            textView20.text = "Live Bonus Time Status = ${it}"
        })

        model.getLiveAuctionList().observe(this, {
            textView21.text = "Live Auction List = ${it}"
        })




        val demoList = ArrayList<LiveRoutesListItem>()

        initAuc.setOnClickListener {
            if (model.getLiveTruckDataList().value.isNullOrEmpty()) {
                Toast.makeText(
                    this,
                    "Please wait while the Live Truck Data is fetched",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                model.scheduleAuction(CurrDateTimeInMillis())
                model.addMandiRoutes(
                    mandiSrc = "a",
                    routesListToUpload = hashMapOf(
                        "b" to
                                hashMapOf(
                                    "Req" to 35,
                                    "Got" to 0,
                                    "Rate" to 6969,
                                )))
                model.scheduleAuction(CurrDateTimeInMillis())
                Toast.makeText(
                    this,
                    "New auction scheduled, now initializing it...",
                    Toast.LENGTH_LONG
                ).show()
                var perUserBidDurationInMillis = 25000L
                model.initializeAuction(
                    status = "Scheduled",
                    startTime = liveAuctionStartTime,
                    perUserBidDurationInMillis = perUserBidDurationInMillis)
                Toast.makeText(
                    this,
                    "New auction initialized!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        monitorAuc.setOnClickListener {
            model.monitorAuction()
        }
        closeAuc.setOnClickListener {
            model.closeAuction()
        }
    }

}