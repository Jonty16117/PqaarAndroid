package com.pqaar.app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.model.Route
import com.pqaar.app.ui.adapters.RouteListAdapter

class RoutesFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val demoRoutesList = ArrayList<Route>()
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                10,
                6,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                10,
                10,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                10,
                7,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                35,
                16,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                15,
                0,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                17,
                5,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                14,
                7,
            )
        )
        demoRoutesList.add(
            Route(
                "Pick Up Location",
                "Drop Location",
                13,
                2,
            )
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView?.adapter = RouteListAdapter(requireActivity(), demoRoutesList)
        recyclerView?.layoutManager = LinearLayoutManager(context)
    }
}