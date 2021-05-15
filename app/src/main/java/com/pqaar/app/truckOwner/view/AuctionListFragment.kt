package com.pqaar.app.truckOwner.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.Bid
import com.pqaar.app.model.LiveAuctionListItem
import com.pqaar.app.truckOwner.adapters.AuctionListAdapter
import com.pqaar.app.truckOwner.viewModel.TruckOwnerViewModel

class AuctionListFragment : Fragment() {
    private val TAG = "AuctionListFragment"

    private var liveAuctionList = ArrayList<LiveAuctionListItem>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_live_auction_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val model = ViewModelProviders.of(this)
            .get(TruckOwnerViewModel::class.java)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = AuctionListAdapter(liveAuctionList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        model.getLiveAuctionList().observe(this, { liveAuctionListDTO ->
            liveAuctionList = ArrayList<LiveAuctionListItem>()
            liveAuctionListDTO.forEach {
                liveAuctionList.add(it.value)
            }

            liveAuctionList = ArrayList(liveAuctionList.sortedWith(compareBy {
                it.CurrNo!!.toInt()
            }))
            Log.d(TAG, "live auction list: ${liveAuctionList}")
            adapter.updateList(liveAuctionList)
            adapter.notifyDataSetChanged()
        })

    }

}