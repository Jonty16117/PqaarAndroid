package com.pqaar.app.pahunchAdmin.view

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.pqaar.app.R
import com.pqaar.app.common.LoginActivity
import com.pqaar.app.model.PahunchTicket
import com.pqaar.app.pahunchAdmin.adapters.PahunchHistoryAdapter
import com.pqaar.app.pahunchAdmin.viewModel.PahunchAdminViewModel
import com.pqaar.app.pahunchAdmin.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.pahunchAdmin.repository.PahunchAdminRepo
import com.pqaar.app.truckOwner.view.AddTruckFragment
import com.pqaar.app.truckOwner.view.RemoveTruckFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@SuppressLint("SetTextI18n")
class PahunchAdminDashboard : AppCompatActivity() {
    private val TAG = "PahunchAdminDashboard"

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var incomingTrucks: TextView
    private lateinit var totalAcceptedTrucks: TextView
    private lateinit var totalRejectedTrucks: TextView
    private lateinit var viewPager: ViewPager
    private lateinit var menu: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var alertDialog: AlertDialog
    private lateinit var pahunchHistoryAdapter: PahunchHistoryAdapter
    private lateinit var model: PahunchAdminViewModel
    private lateinit var closeButton: Button


    private var pahunchHistory = ArrayList<PahunchTicket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pahunch_admin_dashboard)

        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        incomingTrucks = findViewById(R.id.title_text)
        totalAcceptedTrucks = findViewById(R.id.title_text5)
        totalRejectedTrucks = findViewById(R.id.title_text8)
        viewPager = findViewById(R.id.view_pager)
        menu = findViewById(R.id.menu)
        progressBar = findViewById(R.id.progressBar3)
        closeButton = findViewById(R.id.button)

        menu.setOnClickListener { showPopUpMenu(menu) }
        viewPager.adapter = BottomSheetViewPagerAdapter(supportFragmentManager)
        closeButton.setOnClickListener {
            val alertDialog: AlertDialog
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            builder.setTitle("Exit")
            builder.setMessage("Are you sure you want to exit?")
            builder.setIcon(R.drawable.ic_close_dark)
            builder.setPositiveButton("Yes") { dialogInterface, _ ->
                dialogInterface.dismiss()
                finish()
                System.exit(0)
            }
            builder.setNegativeButton("No") { dialogInterface, _ ->
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

        this.model = ViewModelProviders.of(this)
            .get(PahunchAdminViewModel::class.java)


        pahunchHistoryAdapter = PahunchHistoryAdapter(this, pahunchHistory)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = pahunchHistoryAdapter

        GlobalScope.launch(Dispatchers.IO) {
            model.refreshHistory()
        }

        model.getPahunchHistory().observe(this, { pahunchTickets ->
            progressBar.isVisible = true
            pahunchHistory.clear()
            var acceptedCount = 0
            var rejectedCount = 0
            pahunchTickets.forEach { pahunchTicket ->
                if(pahunchTicket.Status == "Accepted") {
                    acceptedCount++
                } else {
                    rejectedCount++
                }
                pahunchHistory.add(pahunchTicket)
            }
            totalAcceptedTrucks.text = acceptedCount.toString()
            totalRejectedTrucks.text = rejectedCount.toString()
            pahunchHistoryAdapter.notifyItemRangeChanged(0,pahunchHistory.size)
            progressBar.isVisible = false
        })

        model.getIncomingTrucks().observe(this, { truckRequests ->
            var truckRequestsCount = 0
            truckRequests.forEach { truckRequest ->
                truckRequestsCount++
            }
            incomingTrucks.text = truckRequestsCount.toString()
        })
    }

    private fun showPopUpMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_pahunch_admin_dashboard)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                /**
                 * Logout
                 */
                R.id.header1 -> {
                    GlobalScope.launch(Dispatchers.IO) {
                        model.refreshHistory()
                    }
                }

                /**
                 * Logout
                 */
                R.id.header2 -> {
                    //show alert dialog box before logging out
                    val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    builder.setTitle("Logout")
                    builder.setMessage("Are you sure you want to logout?")
                    builder.setIcon(R.drawable.ic_logout)
                    builder.setPositiveButton("Logout") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@PahunchAdminDashboard, LoginActivity::class.java)
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

    override fun onBackPressed() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
        builder.setTitle("Exit")
        builder.setMessage("Are you sure you want to exit?")
        builder.setIcon(R.drawable.ic_close_dark)
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            super.onBackPressed()
        }
        builder.setNegativeButton("No") { dialogInterface, _ ->
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