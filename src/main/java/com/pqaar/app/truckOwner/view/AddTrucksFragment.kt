package com.pqaar.app.truckOwner.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R
import com.pqaar.app.truckOwner.adapters.AddTrucksAdapter


class AddTrucksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_trucks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val demoTrucksList = ArrayList<String>()
        demoTrucksList.add("pb3013123f")
        demoTrucksList.add("pb3034232z")
        demoTrucksList.add("pb3453454u")
        demoTrucksList.add("pb5436756n")
        demoTrucksList.add("pb3455756c")
        demoTrucksList.add("pb7895786p")
        demoTrucksList.add("pb6796735o")
        demoTrucksList.add("pb4564578k")
        demoTrucksList.add("pb4564385j")
        demoTrucksList.add("pb4564564f")
        demoTrucksList.add("pb3456224s")
        demoTrucksList.add("pb8909475z")
        demoTrucksList.add("pb5780575v")
        demoTrucksList.add("pb3465398f")
        demoTrucksList.add("pb0789784q")
        demoTrucksList.add("pb7957566k")
        demoTrucksList.add("pb0585673h")

        val textViewTotalTrucks = view.findViewById<TextView>(R.id.trucks_added_count)
        textViewTotalTrucks.text = demoTrucksList.size.toString()

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AddTrucksAdapter(context, demoTrucksList)
        }
    }
}