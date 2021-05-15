package com.pqaar.app.pahunchAdmin.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.LiveTruckDataItem
import com.pqaar.app.pahunchAdmin.adapters.LiveRequestsAdapter
import com.pqaar.app.pahunchAdmin.viewModel.PahunchAdminViewModel

class TruckRequestsFragment: Fragment() {

    private var liveRequests = ArrayList<LiveTruckDataItem>()
    private lateinit var liveRequestsAdapter: LiveRequestsAdapter
    private lateinit var model: PahunchAdminViewModel
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_truck_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        model = ViewModelProviders.of(this)
            .get(PahunchAdminViewModel::class.java)

        liveRequestsAdapter = LiveRequestsAdapter(liveRequests, requireActivity(), this)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = liveRequestsAdapter

        model.getIncomingTrucks().observe(this, {
            liveRequests.clear()
            liveRequests.addAll(it)
            liveRequestsAdapter.notifyDataSetChanged()
        })
    }
}