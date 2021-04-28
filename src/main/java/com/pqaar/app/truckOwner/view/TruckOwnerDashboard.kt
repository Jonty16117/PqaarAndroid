package com.pqaar.app.truckOwner.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.pqaar.app.R
import com.pqaar.app.common.LoginActivity
import com.pqaar.app.common.RegisterUserFragment
import com.pqaar.app.model.TruckHistory
import com.pqaar.app.truckOwner.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.truckOwner.adapters.TruckDriverHistoryAdapter
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel

class TruckOwnerDashboard : AppCompatActivity() {
    private val TAG = "TruckOwnerDashboard"

    private lateinit var alertDialog: AlertDialog
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
        val menu = findViewById<Button>(R.id.menu)

        textViewTotalTrucksOnBid.text = trucksHistoryList.size.toString()
        textViewTotalTrucks.text = "/6 On Bid"
        
        menu.setOnClickListener { showPopUpMenu(menu) }
        
        viewPager.adapter = BottomSheetViewPagerAdapter(supportFragmentManager)
        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

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
                    val logoutBtn = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    logoutBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                    val cancelBtn = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    cancelBtn.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
                }
            }
            true
        }
        popup.show()
    }
}
