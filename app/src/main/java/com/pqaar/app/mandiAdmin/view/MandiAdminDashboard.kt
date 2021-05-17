package com.pqaar.app.mandiAdmin.view

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.pqaar.app.R
import com.pqaar.app.common.LoginActivity
import com.pqaar.app.mandiAdmin.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.mandiAdmin.adapters.MandiAdminRoutesHistoryAdapter
import com.pqaar.app.mandiAdmin.viewModel.MandiAdminViewModel
import com.pqaar.app.truckOwner.view.AddTruckFragment
import com.pqaar.app.truckOwner.view.RemoveTruckFragment

@SuppressLint("SetTextI18n")
class MandiAdminDashboard : AppCompatActivity() {
    private val TAG = "MandiAdminDashboard"

    private lateinit var title_text2: TextView
    private lateinit var title_text: TextView
    private lateinit var history_list: RecyclerView
    private lateinit var viewPager: ViewPager
    private lateinit var menu: Button
    private lateinit var closeButton: Button

    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mandi_admin_dashboard)

        title_text2 = findViewById(R.id.title_text2)
        title_text = findViewById(R.id.title_text)
        history_list = findViewById(R.id.history_list)
        viewPager = findViewById(R.id.view_pager)
        menu = findViewById(R.id.menu)
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

        val adapterList = arrayListOf<MandiAdminRoutesHistoryAdapter>()
        val concatAdapter = ConcatAdapter(adapterList)
        history_list.layoutManager = LinearLayoutManager(baseContext)
        history_list.adapter = concatAdapter

        val model = ViewModelProviders.of(this)
            .get(MandiAdminViewModel::class.java)

        // Set the dashboard text
        model.getMandi().observe(this, {
            title_text2.text = "Mandi ${it}"
            Log.d(TAG, "Fetched mandi name: ${it.toString()}")
        })

        model.getRoutesHistory().observe(this, {
            if (it.size > 0){
                title_text.text = it[0].Routes.size.toString()
                Log.d(TAG, "Updated routes list: ${it}")
                adapterList.forEach { adapter -> concatAdapter.removeAdapter(adapter) }
                adapterList.clear()
                it.forEachIndexed { index, inner_it ->
                    adapterList.add(MandiAdminRoutesHistoryAdapter(inner_it))
                }
                adapterList.forEach { adapter -> concatAdapter.addAdapter(adapter) }
                concatAdapter.notifyDataSetChanged()
            } else {
                title_text.text = "0"
            }
        })
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


    private fun showPopUpMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_mandi_admin_dashboard)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                //Logout
                R.id.header1 -> {
                    //show alert dialog box before logging out
                    val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                    builder.setTitle("Logout")
                    builder.setMessage("Are you sure you want to logout?")
                    builder.setIcon(R.drawable.ic_logout)
                    builder.setPositiveButton("Logout") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this@MandiAdminDashboard, LoginActivity::class.java)
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