package com.pqaar.app.ui.TruckOwner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.pqaar.app.R
import com.pqaar.app.repositories.UnionAdminRepo
import com.pqaar.app.ui.UnionAdmin.UnionAdminDashboard
import com.pqaar.app.ui.common.RegisterUserFragment
import com.pqaar.app.utils.TimeConversions
import com.pqaar.app.viewmodels.TruckOwnerViewModel
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.*

class TruckOwnerDashboardTest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_truck_owner_dashboard_test)


        val truckOwnerData = findViewById<TextView>(R.id.truckOwnerData)
        val liveTruckDataList = findViewById<TextView>(R.id.liveTruckDataList)
        val liveAuctionStatus = findViewById<TextView>(R.id.liveAuctionStatus)
        val liveAuctionStartTime = findViewById<TextView>(R.id.liveAuctionStartTime)
        val liveAuctionEndTime = findViewById<TextView>(R.id.liveAuctionEndTime)
        val liveAuctionBonusTimeInfo = findViewById<TextView>(R.id.liveAuctionBonusTimeInfo)
        val liveRoutesList = findViewById<TextView>(R.id.liveRoutesList)
        val liveAuctionList = findViewById<TextView>(R.id.liveAuctionList)
        val truckNo = findViewById<EditText>(R.id.truckNo)
        val src = findViewById<EditText>(R.id.src)
        val des = findViewById<EditText>(R.id.des)
        val closeAucTO = findViewById<Button>(R.id.closeAucTO)
        val manage_trucks = findViewById<Button>(R.id.manage_trucks)

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        model.getTruckOwnerData().observe(this, {
            truckOwnerData.text = it.toString()
        })

        model.getLiveTruckDataList().observe(this, {
            liveTruckDataList.text = it.toString()
        })

        model.getAuctionStatus().observe(this, {
            liveAuctionStatus.text = it
        })

        model.getAuctionStartTime().observe(this, {
            liveAuctionStartTime.text = TimeConversions.MillisToTimestamp(it)
        })

        model.getAuctionEndTime().observe(this, {
            liveAuctionEndTime.text = TimeConversions.MillisToTimestamp(it)
        })

        model.getAuctionsBonusTimeInfo().observe(this, {
            liveAuctionBonusTimeInfo.text = it.toString()
        })

        model.getLiveRoutesList().observe(this, {
            liveRoutesList.text = it.toString()
        })

        model.getLiveAuctionList().observe(this, {
            liveAuctionList.text = it.toString()
        })

        val activity = this
        closeAucTO.setOnClickListener {
            GlobalScope.launch {
                val job = async{
                    model.bookRoute(truckNo.text.trim().toString(),
                        src.text.trim().toString(),
                        des.text.trim().toString())
                }
                job.await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        activity,
                        "Truck booked successfully!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }

        manage_trucks.setOnClickListener {
            startActivity(Intent(
                activity,
                ManageTrucksTest::class.java
            ).apply { putExtra("null", "null") })
        }
    }
}