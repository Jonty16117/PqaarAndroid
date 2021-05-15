@file:Suppress("PrivatePropertyName")

package com.pqaar.app.truckOwner.view

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.pqaar.app.R
import com.pqaar.app.common.LoginActivity
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.truckOwner.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.truckOwner.adapters.TruckDriverHistoryAdapter
import com.pqaar.app.truckOwner.repository.TruckOwnerRepo
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel
import com.pqaar.app.utils.TimeConversions.CurrDateTimeInMillis

@SuppressLint("SetTextI18n")
class TruckOwnerDashboard : AppCompatActivity() {
    private val TAG = "TruckOwnerDashboard"

    private lateinit var textViewTotalTrucks: TextView
    private lateinit var auctionStatus: TextView
    private lateinit var totalTrucksClosedCountText: TextView
    private lateinit var totalTrucksClosedText: TextView
    private lateinit var totalAuctionTrucks: TextView
    private lateinit var totalRoutesLeft: TextView
    private lateinit var dashboardTimer: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewPager: ViewPager
    private lateinit var menu: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var alertDialog: AlertDialog
    private lateinit var historyAdapter: TruckDriverHistoryAdapter
    private lateinit var model: TruckOwnerViewModel

    private var trucksHistoryList = ArrayList<LiveTruckDataItem>()
    private var liveAuctionList = ArrayList<LiveAuctionListItem>()
    private var liveRoutesList = ArrayList<LiveRoutesListItem>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_truck_owner_dashboard)

        textViewTotalTrucks = findViewById(R.id.title_text9)
        auctionStatus = findViewById(R.id.title_text2)
        totalTrucksClosedCountText = findViewById(R.id.title_text4)
        totalTrucksClosedText = findViewById(R.id.title_text3)
        totalAuctionTrucks = findViewById(R.id.title_text8)
        totalRoutesLeft = findViewById(R.id.title_text5)
        dashboardTimer = findViewById(R.id.title_text)
        recyclerView = findViewById(R.id.historyRecyclerView)
        viewPager = findViewById(R.id.view_pager)
        menu = findViewById(R.id.menu)
        progressBar = findViewById(R.id.progressBar3)
        
        menu.setOnClickListener { showPopUpMenu(menu) }
        
        viewPager.adapter = BottomSheetViewPagerAdapter(supportFragmentManager)
        this.model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        historyAdapter = TruckDriverHistoryAdapter(this, trucksHistoryList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter

        model.liveAuctionStatusChanged.observe(this, {
            /**
             * This observer initializes other observers in the viewmodel that updates
             * the dashboard timer, so DO NOT REMOVE THIS OBSERVER
             */
            Log.d(TAG, "Activity resumed observing viewmodel")
        })

        model.dashboardStatus.observe(this, {
            auctionStatus.text = it
        })

        model.dashboardTimer.observe(this, {
            dashboardTimer.text = it
        })

        model.getLiveAuctionList().observe(this, { LiveAuctionList ->
            progressBar.isVisible = true
            liveAuctionList = ArrayList()
            liveAuctionList.addAll(LiveAuctionList.values)
            liveAuctionList = ArrayList(liveAuctionList.sortedWith(compareBy {
                it.CurrNo!!.toInt()
            }))
            totalAuctionTrucks.text = liveAuctionList.size.toString()

            //Count total trucks which closed their bids
            var totalClosed = 0
            liveAuctionList.forEach {
                if (it.Closed == "true") { totalClosed += 1 }
            }
            val text1 = "$totalClosed"
            totalTrucksClosedCountText.text = text1

            progressBar.isVisible = false
        })

        model.getLiveTruckDataList().observe(this, { liveTruckDataListItem ->
            progressBar.isVisible = true
            trucksHistoryList.clear()
            trucksHistoryList.addAll(liveTruckDataListItem.values)
            updateMyTrucksAdapter()
            progressBar.isVisible = false
        })

        model.getLiveRoutesList().observe(this, { liveRoutesListDTO ->
            var routesLeft = 0
            liveRoutesList.clear()
            liveRoutesListDTO.forEach { liveRoutesListDTOItem ->
                val routes = ArrayList<LiveRoutesListItem.RouteDestination>()
                liveRoutesListDTOItem.value.desData.forEach { route ->
                    val req = route.value["Req"]!!.toInt()
                    val got = route.value["Got"]!!.toInt()
                    val rate = route.value["Rate"]!!.toInt()

                    routes.add(LiveRoutesListItem.RouteDestination(
                        Des = route.key,
                        Req = req,
                        Got = got,
                        Rate = rate
                    ))
                    //Count remaining routes
                    routesLeft += (req - got)
                }
                liveRoutesList.add(LiveRoutesListItem(
                    Mandi = liveRoutesListDTOItem.key,
                    Routes = routes
                ))
                totalRoutesLeft.text = routesLeft.toString()
            }
        })

    }

    private fun updateMyTrucksAdapter() {
        historyAdapter.notifyDataSetChanged()
    }

    private fun showPopUpMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_truck_owner_dashboard)
        
        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {

                //Add Truck
                R.id.header1 -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(android.R.id.content, AddTruckFragment())
                        .addToBackStack(null)
                        .commit()
                }

                //Remove Truck
                R.id.header2 -> {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(android.R.id.content, RemoveTruckFragment())
                        .addToBackStack(null)
                        .commit()
                }

                //Logout
                R.id.header3 -> {
                    //show alert dialog box before logging out
                    val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    builder.setTitle("Logout")
                    builder.setMessage("Are you sure you want to logout?")
                    builder.setIcon(R.drawable.ic_logout)
                    builder.setPositiveButton("Logout") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        val intent = Intent(this@TruckOwnerDashboard, LoginActivity::class.java)
                        try {
                            startActivity(intent)
                        } finally {
                            finish()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    alertDialog = builder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()

                    val messageText = alertDialog.findViewById<TextView>(android.R.id.message)
                    val logoutBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    val cancelBtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    logoutBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    cancelBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    messageText?.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                }
            }
            true
        }
        popup.show()
    }
}
