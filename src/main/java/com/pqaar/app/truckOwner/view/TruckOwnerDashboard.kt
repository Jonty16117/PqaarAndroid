package com.pqaar.app.truckOwner.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.truckOwner.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.truckOwner.adapters.TruckDriverHistoryAdapter
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel

class TruckOwnerDashboard : AppCompatActivity() {

    private var trucksHistoryList = ArrayList<TruckHistory>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_truck_owner_dashboard)

        val textViewTotalTrucksOnBid = findViewById<TextView>(R.id.title_text8)
        val textViewTotalTrucks = findViewById<TextView>(R.id.title_text9)
        val schAucInfo = findViewById<TextView>(R.id.title_text2)
        val dashboardTimer = findViewById<TextView>(R.id.title_text)
        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)

        viewPager.adapter = BottomSheetViewPagerAdapter(supportFragmentManager)
        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        textViewTotalTrucksOnBid.text = trucksHistoryList.size.toString()
        textViewTotalTrucks.text = "/6 On Bid"

        val adapter = TruckDriverHistoryAdapter(this, trucksHistoryList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        var truckNo: String
        var status: String
        var currentListNo: String
        var timestamp: String
        var owner: String
        var route: Pair<String, String>

        model.getLiveTruckDataList().observe(this, { liveTruckDataListItem ->
            trucksHistoryList.clear()
            liveTruckDataListItem.forEach{
                trucksHistoryList.add(it.value)
            }
            adapter.notifyDataSetChanged()
        })
    }
}
