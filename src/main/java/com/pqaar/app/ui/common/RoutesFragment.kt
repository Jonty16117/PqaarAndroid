package com.pqaar.app.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.LiveRoutesListItem
import com.pqaar.app.ui.adapters.RouteListAdapter

class RoutesFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val demoList = ArrayList<LiveRoutesListItem>()


        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.adapter = RouteListAdapter(requireActivity(), demoList)
        recyclerView?.layoutManager = LinearLayoutManager(context)
    }
}