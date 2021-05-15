package com.pqaar.app.mandiAdmin.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.pqaar.app.R
import com.pqaar.app.mandiAdmin.adapters.BottomSheetViewPagerAdapter
import com.pqaar.app.mandiAdmin.adapters.MandiAdminRoutesHistoryAdapter
import com.pqaar.app.mandiAdmin.viewModel.MandiAdminViewModel

@SuppressLint("SetTextI18n")
class MandiAdminDashboard : AppCompatActivity() {
    private val TAG = "MandiAdminDashboard"

    private lateinit var title_text2: TextView
    private lateinit var title_text: TextView
    private lateinit var history_list: RecyclerView
    private lateinit var viewPager: ViewPager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mandi_admin_dashboard)

        title_text2 = findViewById(R.id.title_text2)
        title_text = findViewById(R.id.title_text)
        history_list = findViewById(R.id.history_list)
        viewPager = findViewById(R.id.view_pager)

        viewPager.adapter = BottomSheetViewPagerAdapter(supportFragmentManager)

        /*val historyListData = ArrayList<MandiRoutesHistoryItem>()
        val routes = ArrayList<Pair<String, Int>>()
        val route = Pair("FCI", 10)
        val mrhi1 = MandiRoutesHistoryItem(
            Timestamp=1618203688000,
            Status="Live",
            Routes=routes
        )
        routes.add(route)
        routes.add(route)
        routes.add(route)
        routes.add(route)
        routes.add(route)
        historyListData.add(mrhi1)
        historyListData.add(mrhi1)
        historyListData.add(mrhi1)
        historyListData.add(mrhi1)*/

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
            title_text.text = it[0].Routes.size.toString()
            Log.d(TAG, "Updated routes list: ${it}")
            adapterList.forEach { adapter -> concatAdapter.removeAdapter(adapter) }
            adapterList.clear()
            it.forEachIndexed { index, inner_it ->
                adapterList.add(MandiAdminRoutesHistoryAdapter(inner_it))
            }
            adapterList.forEach { adapter -> concatAdapter.addAdapter(adapter) }
            concatAdapter.notifyDataSetChanged()
        })













        /*val demoList = ArrayList<PropRoutesListItem>()

        demoList.add(PropRoutesListItem("fci1", "10"))
        demoList.add(PropRoutesListItem("fci2", "10"))
        demoList.add(PropRoutesListItem("fci3", "10"))
        demoList.add(PropRoutesListItem("fci4", "10"))
        demoList.add(PropRoutesListItem("fci5", "10"))

        model.AddRoutes(demoList)*/
    }
}