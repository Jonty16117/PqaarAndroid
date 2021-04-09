package com.pqaar.app.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pqaar.app.R
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.ui.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.ui.adapters.TruckDriverHistoryAdapter

class TruckOwnerDashboardActivity : AppCompatActivity() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_truck_owner_dashboard)

        val demoTrucksHistoryList = ArrayList<TruckHistory>()
        demoTrucksHistoryList.add(TruckHistory(
                "pb3013123f", 'a', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'r', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'a', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'r', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'a', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'a', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'r', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))
        demoTrucksHistoryList.add(TruckHistory(
            "pb3013123f", 'r', "Pick Up Location",
            "24/08/2021", "43:21", "Drop Location",
            "24/08/2021", "14:43:21", 696))


        val textViewTotalTrucksOnBid = findViewById<TextView>(R.id.title_text8)
        val textViewTotalTrucks = findViewById<TextView>(R.id.title_text9)

        textViewTotalTrucksOnBid.text = demoTrucksHistoryList.size.toString()
        textViewTotalTrucks.text = "/6 On Bid"

        val recyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TruckDriverHistoryAdapter(this, demoTrucksHistoryList)

        findViewById<ViewPager>(R.id.view_pager).adapter = BottomSheetViewPagerAdapter(supportFragmentManager)

        /*bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet))


        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //Not yet implemented
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        window.decorView.setBackgroundColor(ContextCompat.getColor(this@TruckOwnerDashboardActivity, R.color.white
                        ))
                    }
                    else -> {
                        window.decorView.setBackgroundColor(ContextCompat.getColor(this@TruckOwnerDashboardActivity, R.color.colorPrimary))
                    }
                }
            }
        })*/

    }
}
