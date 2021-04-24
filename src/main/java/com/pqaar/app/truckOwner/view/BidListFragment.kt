package com.pqaar.app.truckOwner.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.Bid
import com.pqaar.app.truckOwner.adapters.BidListAdapter

class BidListFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_bid_list, container, false)

        val demoBidList = ArrayList<Bid>()
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'o', false))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'a', false))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'o', true))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'r', false))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'a', true))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'r', true))
        demoBidList.add(
            Bid(1,696, "PB30XXXX", "Source",
                "Destination", 'r', false))

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = BidListAdapter(requireActivity(), demoBidList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

}