package com.pqaar.app.ui.MandiAdmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.pqaar.app.R
import com.pqaar.app.model.PropRoutesListItem
import com.pqaar.app.viewmodels.MandiAdminViewModel
import com.pqaar.app.viewmodels.UnionAdminViewModel

class MandiAdminDashboard : AppCompatActivity() {
    private val TAG = "MandiAdminDashboard"

    private lateinit var textView14: TextView
    private lateinit var textView15: TextView
    private lateinit var textView16: TextView
    private lateinit var textView19: TextView
    private lateinit var textView20: TextView
    private lateinit var button2: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mandi_admin_dashboard)

        textView14 = findViewById(R.id.textView14)
        textView15 = findViewById(R.id.textView15)
        textView16 = findViewById(R.id.textView16)
        textView19 = findViewById(R.id.textView19)
        textView20 = findViewById(R.id.textView20)
        button2 = findViewById(R.id.button2)

        //model is an object of mandiadminviewmodel
        val model = ViewModelProviders.of(this)
            .get(MandiAdminViewModel::class.java)

        val demoList = ArrayList<PropRoutesListItem>()

        demoList.add(PropRoutesListItem("fci1", "10"))
        demoList.add(PropRoutesListItem("fci2", "10"))
        demoList.add(PropRoutesListItem("fci3", "10"))
        demoList.add(PropRoutesListItem("fci4", "10"))
        demoList.add(PropRoutesListItem("fci5", "10"))

        model.AddRoutes(demoList)
    }
}