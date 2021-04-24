package com.pqaar.app.truckOwner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pqaar.app.R

class AddTrucksAdapter(private val context: Context, private val truckNumbersList: ArrayList<String>) :
    RecyclerView.Adapter<AddTrucksAdapter.AddTrucksViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddTrucksViewHolder {
        return AddTrucksViewHolder(
                LayoutInflater.from(context)
                        .inflate((R.layout.truck_item), parent,
                        false))
    }

    override fun onBindViewHolder(holder: AddTrucksViewHolder, position: Int) {
        holder.setData(truckNumbersList[position], position)
    }

    override fun getItemCount(): Int {
        return truckNumbersList.size
    }

    inner class AddTrucksViewHolder(private val truckItemView: View) : RecyclerView.ViewHolder(truckItemView) {

        fun setData(truckNumber: String, position: Int) {
            truckItemView.findViewById<TextView>(R.id.truck_no).text = (position + 1).toString()
            truckItemView.findViewById<TextView>(R.id.textView3).text = truckNumber
        }

    }

}